package com.xiaopo.flying.poicamera.poicamera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.xiaopo.flying.poicamera.util.AutoFocusHelper;
import com.xiaopo.flying.poicamera.util.CameraUtil;

import java.io.File;
import java.util.Arrays;

/**
 * Created by snowbean on 16-7-29.
 */

public class PoiCamera implements CameraPrototype {

    private static final String TAG = "PoiCamera";

    @Override
    public void setOnPreviewStartListener(OnPreviewStartListener onPreviewStartListener) {
        mOnPreviewStartListener = onPreviewStartListener;
    }


    public enum RequestTag {
        Capture,
        Preview,
    }

    //the time needed to resume continuous focus mode after we tab to focus
    private static final int DELAY_TIME_RESUME_CONTINUOUS_AF = 5000;

    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCameraCharacteristics;
    private CameraCaptureSession mCaptureSession;

    //the current surface to preview
    private Surface mPreviewSurface;

    //camera thread to handle preview,focus,capture,etc
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;

    //handler to send callback to main thread
    private Handler mMainHandler;

    //to receive image data after capture
    private ImageReader mImageReader;
    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable: poicamera");

            final ImageSaveTask saveTask = new ImageSaveTask() {
                @Override
                protected void onPostExecute(File file) {
                    super.onPostExecute(file);
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mPhotoCaptureParameters != null && mPhotoCaptureParameters.getPhotoCaptureCallback() != null) {
                                mPhotoCaptureParameters.getPhotoCaptureCallback().onPhotoSaved();
                            }
                        }
                    });
                }
            };

            saveTask.execute(new ImageData(reader.acquireLatestImage(), mSaveFile));

        }
    };

    //the current output picture size
    private Size mPictureSize;

    private int mMaxFaceDetectMode = CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_OFF;

    //current auto focus mode,when we tap to focus, the mode will switch to auto
    private AutoFocusMode mControlAFMode = AutoFocusMode.CONTINUOUS_PICTURE;

    //focus zero region
    private static final MeteringRectangle[] ZERO_WEIGHT_3A_REGION = AutoFocusHelper.getZeroWeightRegion();
    private MeteringRectangle[] mAFRegions = ZERO_WEIGHT_3A_REGION;
    private MeteringRectangle[] mAERegions = ZERO_WEIGHT_3A_REGION;

    private float mZoomValue = 1f;

    // Current crop region: set from mZoomValue.
    private Rect mCropRegion;

    private boolean mIsReadyToCapture = false;

    private boolean mTakePictureUtilReady;

    private Runnable mTakePictureRunnable;

    private File mSaveFile;

    private PhotoCaptureParameters mPhotoCaptureParameters;

    private AutoFocusState mLastFocusState = AutoFocusState.INACTIVE;

    private OnFocusStateChangeListener mOnFocusStateChangeListener;

    private OnPreviewStartListener mOnPreviewStartListener;

    private FlashMode mFlashMode = FlashMode.AUTO;

    private CameraCaptureSession.CaptureCallback mFocusStateListener = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureRequest request, CaptureResult result) {
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            afState = afState == null ? CaptureResult.CONTROL_AF_STATE_INACTIVE : afState;

            final AutoFocusState state = AutoFocusState.fromCamera2State(afState);

//            Log.d(TAG, "process: state->"+state.name());

            if (mLastFocusState != state) {
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnFocusStateChangeListener != null) {
                            mOnFocusStateChangeListener.onFocusStateChanged(state, mControlAFMode);
                        }
                    }
                });
            }

            mIsReadyToCapture = state == AutoFocusState.ACTIVE_FOCUSED ||
                    state == AutoFocusState.ACTIVE_UNFOCUSED ||
                    state == AutoFocusState.PASSIVE_FOCUSED ||
                    state == AutoFocusState.PASSIVE_UNFOCUSED;


            if (mTakePictureUtilReady && mIsReadyToCapture) {
                mCameraHandler.post(mTakePictureRunnable);
                mTakePictureUtilReady = false;
            }

            mLastFocusState = state;

        }


        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                     @NonNull CaptureRequest request,
                                     long timestamp,
                                     long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            process(request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(request, result);

        }
    };

    //the runnable to resume continuous focus mode after tab to focus
    private Runnable mResumePreviewRunnable = new Runnable() {
        @Override
        public void run() {
            mAERegions = ZERO_WEIGHT_3A_REGION;
            mAFRegions = ZERO_WEIGHT_3A_REGION;
            mControlAFMode = AutoFocusMode.CONTINUOUS_PICTURE;
            if (mCameraDevice != null)
                sendRepeatPreviewRequest();
        }
    };


    public PoiCamera(
            CameraDevice cameraDevice, CameraCharacteristics cameraCharacteristics, Size pictureSize, Handler mainHandler) {
        mCameraDevice = cameraDevice;
        mCameraCharacteristics = cameraCharacteristics;
        mPictureSize = pictureSize;
        mMainHandler = mainHandler;

        mCameraThread = new HandlerThread("PoiCamera");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

        mImageReader = ImageReader.newInstance(
                pictureSize.getWidth(), pictureSize.getHeight(), ImageFormat.JPEG, 2);

        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mCameraHandler);

        obtainCameraInfo();
    }

    private void obtainCameraInfo() {
        int[] faceDetectModes = mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);

        if (faceDetectModes == null) return;

        switch (faceDetectModes.length) {
            case 1:
                mMaxFaceDetectMode = CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_OFF;
                break;
            case 2:
                mMaxFaceDetectMode = CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE;
                break;
            default:
                mMaxFaceDetectMode = CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_FULL;
                break;
        }
    }

    @Override
    public void startPreview(Surface surface) {
        mPreviewSurface = surface;
        startPreviewAsync(surface);
    }

    private void startPreviewAsync(final Surface surface) {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                setupPreview(surface);
            }
        });
    }

    private void setupPreview(Surface surface) {
        try {
            if (mCaptureSession != null) {
                mCaptureSession.abortCaptures();
                mCaptureSession = null;
            }


            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mCaptureSession = session;

                            mAFRegions = ZERO_WEIGHT_3A_REGION;
                            mAERegions = ZERO_WEIGHT_3A_REGION;

                            mZoomValue = 1f;
                            mCropRegion = cropRegionForZoom(mZoomValue);

                            boolean isSuccessed = sendRepeatPreviewRequest();

                            if (isSuccessed && mOnPreviewStartListener != null) {
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOnPreviewStartListener.onPreviewStarted();
                                    }
                                });
                            } else {
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mOnPreviewStartListener.onPreviewFailed();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, mCameraHandler);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean sendRepeatPreviewRequest() {
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mPreviewSurface);
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            builder.setTag(RequestTag.Preview);
            addBaselineCaptureKeysToRequest(builder);

            mCaptureSession.setRepeatingRequest(builder.build(),
                    mFocusStateListener,
                    mCameraHandler);
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addBaselineCaptureKeysToRequest(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_AF_REGIONS, mAFRegions);
        builder.set(CaptureRequest.CONTROL_AE_REGIONS, mAERegions);
        builder.set(CaptureRequest.SCALER_CROP_REGION, mCropRegion);
        builder.set(CaptureRequest.CONTROL_AF_MODE, mControlAFMode.switchToCamera2FocusMode());
        builder.set(CaptureRequest.CONTROL_AE_MODE, mFlashMode.switchToCamera2Mode());
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
        // Enable face detection
        builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                mMaxFaceDetectMode);
        builder.set(CaptureRequest.CONTROL_SCENE_MODE,
                CaptureRequest.CONTROL_SCENE_MODE_FACE_PRIORITY);
    }


    private Rect cropRegionForZoom(float zoom) {
        return AutoFocusHelper.cropRegionForZoom(mCameraCharacteristics, zoom);
    }

    @Override
    public void triggerFocusArea(float x, float y) {
        Integer sensorOrientation = mCameraCharacteristics.get(
                CameraCharacteristics.SENSOR_ORIENTATION);

        sensorOrientation = sensorOrientation == null ? 0 : sensorOrientation;

        mAERegions = AutoFocusHelper.aeRegionsForNormalizedCoord(x, y, mCropRegion, sensorOrientation);
        mAFRegions = AutoFocusHelper.afRegionsForNormalizedCoord(x, y, mCropRegion, sensorOrientation);

        try {
            // Step 1: Request single frame CONTROL_AF_TRIGGER_START.
            CaptureRequest.Builder builder;
            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mPreviewSurface);
            builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            builder.setTag(RequestTag.Preview);

            mControlAFMode = AutoFocusMode.AUTO;

            addBaselineCaptureKeysToRequest(builder);
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            mCaptureSession.capture(builder.build(), mFocusStateListener, mCameraHandler);

            // Step 2: Call repeatingPreview to update mControlAFMode.
            sendRepeatPreviewRequest();
            resumeContinuousAFAfterDelay(DELAY_TIME_RESUME_CONTINUOUS_AF);
        } catch (CameraAccessException ex) {
            Log.e(TAG, "Could not execute preview request.", ex);
        }
    }

    @Override
    public void takePicture(final PhotoCaptureParameters parameters) {
        if (parameters == null) {
            Log.e(TAG, "takePicture: the photo capture param can not be null");
            return;
        }

        mTakePictureRunnable = new Runnable() {
            @Override
            public void run() {
                takePictureNow(parameters);
            }
        };

        //if the camera is front, take picture now, because in some device the focus state is always INACTIVE.
        if (isFrontCamera()) {
            takePictureNow(parameters);
            return;
        }

        if (mLastFocusState == AutoFocusState.ACTIVE_SCAN || mLastFocusState == AutoFocusState.INACTIVE) {
            mTakePictureUtilReady = true;
        } else {
            takePictureNow(parameters);
        }

    }

    private void takePictureNow(PhotoCaptureParameters parameters) {
        mPhotoCaptureParameters = parameters;
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.setTag(RequestTag.Capture);
            addBaselineCaptureKeysToRequest(builder);
            //TODO parameter process

//            builder.set(CaptureRequest.CONTROL_AE_MODE,CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);

            mSaveFile = parameters.getSavedFile();
            builder.set(CaptureRequest.JPEG_ORIENTATION,
                    CameraUtil.getJPEGOrientation(parameters.getOrientation(), mCameraCharacteristics));

            builder.addTarget(mPreviewSurface);
            builder.addTarget(mImageReader.getSurface());


            mCaptureSession.capture(builder.build(),
                    mFocusStateListener,
                    mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void resumeContinuousAFAfterDelay(int timeMillions) {
        mCameraHandler.removeCallbacks(mResumePreviewRunnable);
        mCameraHandler.postDelayed(mResumePreviewRunnable, timeMillions);
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean isFrontCamera() {
        return mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean isBackCamera() {
        return mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK;
    }

    @Override
    public Size[] getSupportSizes() {
        return mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
    }

    @Override
    public Size[] getPreviewSizes() {
        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
            return new Size[0];
        } else {
            return map.getOutputSizes(TextureView.class);
        }

    }

    @Override
    public Size chooseOptimalPreviewSize() {
        return null;
    }

    @Override
    public void setZoom(float zoom) {

    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public float getMaxZoom() {
        return mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean isFlashSupport() {
        return mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
    }

    @Override
    public boolean setFlashMode(FlashMode flashMode) {
        if (isFlashSupport()) {
            mFlashMode = flashMode;
            sendRepeatPreviewRequest();
            return true;
        }
        return false;
    }

    @Override
    public void setOnFocusStateChangeListener(OnFocusStateChangeListener onFocusStateChangeListener) {
        mOnFocusStateChangeListener = onFocusStateChangeListener;
    }

    @Override
    public void close() {
        if (mCaptureSession != null) {
            try {
                mCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

        closeCameraThread();
    }

    private void closeCameraThread() {
        mCameraThread.quitSafely();

        try {
            mCameraThread.join();
            mCameraThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
