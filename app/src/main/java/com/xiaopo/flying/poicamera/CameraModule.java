package com.xiaopo.flying.poicamera;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.Size;
import android.view.Surface;

import com.xiaopo.flying.poicamera.ui.custom.AutoFitTextureView;

/**
 * Created by snowbean on 16-7-30.
 */

public class CameraModule implements CameraPrototype {
    private CameraOpenHelper mCameraOpenHelper;
    private PoiCamera mPoiCamera;

    private AutoFitTextureView mTextureView;
    private Size mPreviewSize;

    private OnFocusStateChangeListener mOnFocusStateChangeListener;
    private OnPreviewStartListener mOnPreviewStartListener;

    private int mWhichCamera = CameraOpenHelper.REAR_CAMERA;


    public CameraModule(Activity activity, final AutoFitTextureView textureView, Handler mainHandler) {
        mTextureView = textureView;

        mCameraOpenHelper = new CameraOpenHelper(activity, textureView, mainHandler);
        mCameraOpenHelper.setOnCameraOpenCallback(new CameraOpenHelper.OnCameraOpenCallback() {
            @Override
            public void onCameraOpened(PoiCamera camera, Size previewSize) {
                mPoiCamera = camera;
                mPreviewSize = previewSize;

                mPoiCamera.setOnPreviewStartListener(mOnPreviewStartListener);
                mPoiCamera.setOnFocusStateChangeListener(mOnFocusStateChangeListener);

                SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
                Surface surface = new Surface(surfaceTexture);

                startPreview(surface);
            }

            @Override
            public void onCameraDisconnected() {
                close();
                mPoiCamera = null;
            }

            @Override
            public void onCameraError(int error) {
                close();
                mPoiCamera = null;
            }
        });

    }

    public void resume() {
        openCamera(mWhichCamera);
    }

    private void openCamera(int which) {
        mWhichCamera = which;
        if (mTextureView.isAvailable()) {
            mCameraOpenHelper.openCamera(mPreviewSize.getWidth(), mPreviewSize.getHeight(), which);
        } else {
            mTextureView.setSurfaceTextureListener(mCameraOpenHelper.getSurfaceTextureListener());
        }
    }

    public void pause() {
        close();
    }


    public void switchCamera() {
        if (isBackCamera()) {
            close();
            openCamera(CameraOpenHelper.FRONT_CAMERA);
        } else {
            close();
            openCamera(CameraOpenHelper.REAR_CAMERA);
        }
    }

    @Override
    public void startPreview(Surface surface) {
        if (mPoiCamera != null)
            mPoiCamera.startPreview(surface);
    }

    @Override
    public void triggerFocusArea(float x, float y) {
        if (mPoiCamera != null)
            mPoiCamera.triggerFocusArea(x, y);
    }

    @Override
    public void takePicture(PhotoCaptureParameters parameters) {
        if (mPoiCamera != null)
            mPoiCamera.takePicture(parameters);
    }

    @Override
    public boolean isFrontCamera() {
        return mPoiCamera != null && mPoiCamera.isFrontCamera();
    }

    @Override
    public boolean isBackCamera() {
        return mPoiCamera != null && mPoiCamera.isBackCamera();
    }

    @Override
    public Size[] getSupportSizes() {
        if (mPoiCamera != null)
            return mPoiCamera.getSupportSizes();
        return new Size[0];
    }

    @Override
    public Size[] getPreviewSizes() {
        if (mPoiCamera != null)
            return mPoiCamera.getPreviewSizes();
        return new Size[0];
    }

    @Override
    public Size chooseOptimalPreviewSize() {
        return null;
    }

    @Override
    public void setZoom(float zoom) {
        if (mPoiCamera != null)
            mPoiCamera.setZoom(zoom);
    }

    @Override
    public float getMaxZoom() {
        if (mPoiCamera != null)
            return mPoiCamera.getMaxZoom();
        return -1f;
    }

    @Override
    public boolean isFlashSupport() {
        return mPoiCamera != null && mPoiCamera.isFlashSupport();
    }

    @Override
    public boolean setFlashMode(FlashMode flashMode) {
        return mPoiCamera != null && mPoiCamera.setFlashMode(flashMode);
    }

    @Override
    public void setOnFocusStateChangeListener(OnFocusStateChangeListener onFocusStateChangeListener) {
        mOnFocusStateChangeListener = onFocusStateChangeListener;
    }

    @Override
    public void setOnPreviewStartListener(OnPreviewStartListener onPreviewStartListener) {
        mOnPreviewStartListener = onPreviewStartListener;
    }

    @Override
    public void close() {
        if (mPoiCamera != null)
            mPoiCamera.close();
    }

    public int getWhichCamera() {
        return mWhichCamera;
    }

}
