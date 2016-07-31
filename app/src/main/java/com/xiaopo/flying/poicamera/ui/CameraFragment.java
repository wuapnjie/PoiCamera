package com.xiaopo.flying.poicamera.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.xiaopo.flying.poicamera.CameraModule;
import com.xiaopo.flying.poicamera.CameraOpenHelper;
import com.xiaopo.flying.poicamera.CameraPrototype;
import com.xiaopo.flying.poicamera.PhotoCaptureCallback;
import com.xiaopo.flying.poicamera.PhotoCaptureParameters;
import com.xiaopo.flying.poicamera.R;
import com.xiaopo.flying.poicamera.ui.custom.AutoFitTextureView;
import com.xiaopo.flying.poicamera.util.FileUtil;

import java.io.File;

/**
 * Created by snowbean on 16-7-22.
 */

public class CameraFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "CameraFragment";

    private AutoFitTextureView mTextureView;
    private ImageView mBtnCapture;
    private FrameLayout mPreviewContainer;
    private ImageView mIvFocus;
    private CameraModule mCameraModule;
    private ImageView mBtnSwitchCamera;
    private ImageView mBtnSwitchGridMode;
    private ImageView mBtnSwitchFlashMode;
    private int mFingerX;
    private int mFingerY;

    private CameraPrototype.FlashMode mCurrentFlashMode = CameraPrototype.FlashMode.AUTO;

    public static CameraFragment newInstance() {

        Bundle args = new Bundle();

        CameraFragment fragment = new CameraFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mBtnCapture = (ImageView) view.findViewById(R.id.btn_capture);
        mPreviewContainer = (FrameLayout) view.findViewById(R.id.preview_container);
        mIvFocus = (ImageView) view.findViewById(R.id.iv_focus);

        mBtnSwitchCamera = (ImageView) view.findViewById(R.id.btn_switch_camera);
        mBtnSwitchGridMode = (ImageView) view.findViewById(R.id.btn_switch_grid_mode);
        mBtnSwitchFlashMode = (ImageView) view.findViewById(R.id.btn_switch_flash_mode);

        mBtnCapture.setOnClickListener(this);
        mBtnSwitchCamera.setOnClickListener(this);
        mBtnSwitchGridMode.setOnClickListener(this);
        mBtnSwitchFlashMode.setOnClickListener(this);

        final int length = (int) (getResources().getDisplayMetrics().density * 64);

        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int actionMasked = MotionEventCompat.getActionMasked(event);

                switch (actionMasked) {
                    case MotionEvent.ACTION_DOWN:
                        mFingerX = (int) event.getX();
                        mFingerY = (int) event.getY();
                        Log.d(TAG, "onTouch: x->" + mFingerX + ",y->" + mFingerY);

                        mIvFocus.setX(mFingerX - length / 2);
                        mIvFocus.setY(mFingerY - length / 2);

                        mIvFocus.setVisibility(View.VISIBLE);

                        mCameraModule.triggerFocusArea(mFingerX, mFingerY);
                        mIvFocus.setVisibility(View.VISIBLE);

                        break;
                }

                return false;
            }
        });

        mCameraModule = new CameraModule(getActivity(), mTextureView, new Handler());
        mCameraModule.setOnFocusStateChangeListener(new CameraPrototype.OnFocusStateChangeListener() {
            @Override
            public void onFocusStateChanged(CameraPrototype.AutoFocusState state, CameraPrototype.AutoFocusMode focusMode) {
                if (focusMode == CameraPrototype.AutoFocusMode.AUTO && state == CameraPrototype.AutoFocusState.ACTIVE_FOCUSED) {
                    mIvFocus.setVisibility(View.INVISIBLE);
                }

                //TODO need show trigger focus failed
                if (focusMode == CameraPrototype.AutoFocusMode.AUTO && state == CameraPrototype.AutoFocusState.ACTIVE_UNFOCUSED) {
                    mIvFocus.setVisibility(View.INVISIBLE);
                }

                Log.d(TAG, "onFocusStateChanged: state -> " + state.name());
            }
        });

        mCameraModule.setOnPreviewStartListener(new CameraPrototype.OnPreviewStartListener() {
            @Override
            public void onPreviewStarted() {
                mBtnSwitchCamera.setEnabled(true);
            }

            @Override
            public void onPreviewFailed() {
                Toast.makeText(getContext(), "occur error", Toast.LENGTH_SHORT).show();
                mBtnSwitchCamera.setEnabled(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 110);
            return;
        }

        mCameraModule.resume();
    }

    @Override
    public void onPause() {
        mCameraModule.pause();
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 110 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            mCameraModule.resume();
        }
    }

    //拍照
    private void takePhoto() {
        PhotoCaptureParameters parameters = new PhotoCaptureParameters();
        parameters.setOrientation(getActivity().getWindowManager().getDefaultDisplay().getRotation());
        final File saveFile = FileUtil.getNewFile(getContext(), "PoiCamera");
        parameters.setSavedFile(saveFile);
        parameters.setPhotoCaptureCallback(new PhotoCaptureCallback() {
            @Override
            public void onPhotoSaved() {
                Toast.makeText(getContext(), saveFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();

                FileUtil.notifySystemGallery(getContext(), saveFile);
            }

            @Override
            public void onPhotoCaptureFailed() {

            }
        });

        mCameraModule.takePicture(parameters);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_capture:
                takePhoto();
                break;
            case R.id.btn_switch_camera:
                switchCamera();
                break;
            case R.id.btn_switch_grid_mode:
                switchGridMode();
                break;
            case R.id.btn_switch_flash_mode:
                switchFlashMode();
                break;
        }
    }

    private void switchFlashMode() {
        switch (mCurrentFlashMode) {
            case AUTO:
                mCurrentFlashMode = CameraPrototype.FlashMode.OFF;
                mBtnSwitchFlashMode.setImageResource(R.drawable.ic_flash_off_white_24dp);
                mCameraModule.setFlashMode(CameraPrototype.FlashMode.OFF);
                break;
            case OFF:
                mCurrentFlashMode = CameraPrototype.FlashMode.ON;
                mBtnSwitchFlashMode.setImageResource(R.drawable.ic_flash_on_white_24dp);
                mCameraModule.setFlashMode(CameraPrototype.FlashMode.ON);
                break;
            case ON:
                mCurrentFlashMode = CameraPrototype.FlashMode.AUTO;
                mBtnSwitchFlashMode.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                mCameraModule.setFlashMode(CameraPrototype.FlashMode.AUTO);
                break;

        }
    }

    private void switchGridMode() {

    }

    private void switchCamera() {
        if (mCameraModule.getWhichCamera() == CameraOpenHelper.REAR_CAMERA) {
            mBtnSwitchCamera.setImageResource(R.drawable.ic_camera_rear_white_24dp);
        } else {
            mBtnSwitchCamera.setImageResource(R.drawable.ic_camera_front_white_24dp);
        }
        mCameraModule.switchCamera();

    }

}
