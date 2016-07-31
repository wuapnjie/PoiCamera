package com.xiaopo.flying.poicamera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.xiaopo.flying.poicamera.ui.custom.AutoFitTextureView;
import com.xiaopo.flying.poicamera.util.CameraUtil;
import com.xiaopo.flying.poicamera.util.CompareSizesByArea;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by snowbean on 16-7-29.
 */

public class CameraOpenHelper {
    private static final String TAG = "CameraHelper";
    public static final int FRONT_CAMERA = 0;
    public static final int REAR_CAMERA = 1;

    private Handler mMainHandler;
    private Activity mActivity;
    private AutoFitTextureView mTextureView;

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private OnCameraOpenCallback mOnCameraOpenCallback;
    private Size[] mOutputSizes;
    private Size mCurrentOutputSize;
    private Integer mSensorOrientation;
    private Size mPreviewSize;
    private boolean mFlashSupported;
    private String mCameraId;

    private int mWhichCamera = REAR_CAMERA;

    private CameraCharacteristics mCameraCharacteristics;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            final PoiCamera poiCamera = new PoiCamera(camera, mCameraCharacteristics, mCurrentOutputSize, mMainHandler);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnCameraOpenCallback != null) {
                        mOnCameraOpenCallback.onCameraOpened(poiCamera, mPreviewSize);
                    }
                }
            });
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnCameraOpenCallback != null) {
                        mOnCameraOpenCallback.onCameraDisconnected();
                    }
                }
            });
        }

        @Override
        public void onError(@NonNull CameraDevice camera, final int error) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnCameraOpenCallback != null) {
                        mOnCameraOpenCallback.onCameraError(error);
                    }
                }
            });
        }
    };

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: " + width + "x" + height);
            openCamera(width, height, mWhichCamera);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: " + width + "x" + height);
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    public CameraOpenHelper(Activity activity, AutoFitTextureView textureView, Handler mainHandler) {
        mActivity = activity;
        mTextureView = textureView;
        mMainHandler = mainHandler;

        textureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    public void openCamera(int width, int height, int which) {
        mWhichCamera = which;

        setUpCamera(mTextureView, width, height, which);
        configureTransform(width, height);

        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mMainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCamera(AutoFitTextureView textureView, int width, int height, int which) {
        try {
            CameraManager cameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT && which == REAR_CAMERA) {
                    continue;
                }

                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK && which == FRONT_CAMERA) {
                    continue;
                }

                StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (configurationMap == null) continue;

                mOutputSizes = configurationMap.getOutputSizes(ImageFormat.JPEG);

                //TODO 选取了最大的照片尺寸
                mCurrentOutputSize = Collections.max(Arrays.asList(mOutputSizes), new CompareSizesByArea());

                int displayRotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                mSensorOrientation = mSensorOrientation == null ? 0 : mSensorOrientation;

                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                mActivity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }


//                Size[] previewSizes = configurationMap.getOutputSizes(SurfaceTexture.class);

                //TODO choose preview size
                mPreviewSize = CameraUtil.chooseOptimalPreviewSize(configurationMap.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, mCurrentOutputSize);

//                mPreviewSize = CameraUtil.chooseOptimalPreviewSize(configurationMap.getOutputSizes(SurfaceTexture.class),
//                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                        maxPreviewHeight, 16, 9);

                //TODO    fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = mActivity.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    textureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                mCameraCharacteristics = characteristics;
                mCameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void setOnCameraOpenCallback(OnCameraOpenCallback onCameraOpenCallback) {
        mOnCameraOpenCallback = onCameraOpenCallback;
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize || null == mActivity) {
            return;
        }
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    public TextureView.SurfaceTextureListener getSurfaceTextureListener() {
        return mSurfaceTextureListener;
    }

    public interface OnCameraOpenCallback {
        void onCameraOpened(PoiCamera camera, Size previewSize);

        void onCameraDisconnected();

        void onCameraError(int error);
    }

}
