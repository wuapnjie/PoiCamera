package com.xiaopo.flying.poicamera;

import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.util.Size;
import android.view.Surface;

/**
 * Created by snowbean on 16-7-29.
 */

public interface CameraPrototype {

    enum FlashMode {
        AUTO, ON, OFF;

        int switchToCamera2Mode(){
            switch (this){
                case AUTO:
                    return CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;
                case ON:
                    return CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
                case OFF:
                    return CaptureRequest.CONTROL_AE_MODE_ON;
                default:
                    return CaptureRequest.CONTROL_AE_MODE_ON;
            }
        }
    }

    enum AutoFocusState {
        /**
         * Indicates AF system is inactive for some reason (could be an error).
         */
        INACTIVE,
        /**
         * Indicates active scan in progress.
         */
        ACTIVE_SCAN,
        /**
         * Indicates active scan success (in focus).
         */
        ACTIVE_FOCUSED,
        /**
         * Indicates active scan failure (not in focus).
         */
        ACTIVE_UNFOCUSED,
        /**
         * Indicates passive scan in progress.
         */
        PASSIVE_SCAN,
        /**
         * Indicates passive scan success (in focus).
         */
        PASSIVE_FOCUSED,
        /**
         * Indicates passive scan failure (not in focus).
         */
        PASSIVE_UNFOCUSED;
        
        public static AutoFocusState fromCamera2State(int state){
            switch (state){
                case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
                    return ACTIVE_SCAN;
                case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
                    return PASSIVE_SCAN;
                case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
                    return PASSIVE_FOCUSED;
                case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
                    return ACTIVE_FOCUSED;
                case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
                    return PASSIVE_UNFOCUSED;
                case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                    return ACTIVE_UNFOCUSED;
                default:
                    return INACTIVE;
            }
        }
    }

    enum AutoFocusMode {
        /**
         * System is continuously focusing.
         */
        CONTINUOUS_PICTURE,
        /**
         * System is running a triggered scan.
         */
        AUTO;

        int switchToCamera2FocusMode() {
            switch (this) {
                case CONTINUOUS_PICTURE:
                    return CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
                case AUTO:
                    return CameraMetadata.CONTROL_AF_MODE_AUTO;
                default:
                    return CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
            }
        }
    }

    void startPreview(Surface surface);

    void triggerFocusArea(float x, float y);

    void takePicture(PhotoCaptureParameters parameters);

    boolean isFrontCamera();

    boolean isBackCamera();

    Size[] getSupportSizes();

    Size[] getPreviewSizes();

    Size chooseOptimalPreviewSize();

    void setZoom(float zoom);

    float getMaxZoom();

    boolean isFlashSupport();

    boolean setFlashMode(FlashMode flashMode);

    void setOnFocusStateChangeListener(OnFocusStateChangeListener onFocusStateChangeListener);

    void setOnPreviewStartListener(OnPreviewStartListener onPreviewStartListener);

    void close();

    interface OnFocusStateChangeListener {
        void onFocusStateChanged(AutoFocusState state, AutoFocusMode focusMode);
    }

    interface OnPreviewStartListener{

        void onPreviewStarted();

        void onPreviewFailed();
    }
}
