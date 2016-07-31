package com.xiaopo.flying.poicamera;

import android.location.Location;

import java.io.File;

/**
 * Created by snowbean on 16-7-30.
 */

public class PhotoCaptureParameters {
    private CameraPrototype.FlashMode mFlashMode;
    private int orientation = 0;
    private Location mLocation;
    private int mTimeDelayed = 0;
    private PhotoCaptureCallback mPhotoCaptureCallback;
    private File mSavedFile;

    public CameraPrototype.FlashMode getFlashMode() {
        return mFlashMode;
    }

    public void setFlashMode(CameraPrototype.FlashMode flashMode) {
        mFlashMode = flashMode;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public int getTimeDelayed() {
        return mTimeDelayed;
    }

    public void setTimeDelayed(int timeDelayed) {
        mTimeDelayed = timeDelayed;
    }

    public PhotoCaptureCallback getPhotoCaptureCallback() {
        return mPhotoCaptureCallback;
    }

    public void setPhotoCaptureCallback(PhotoCaptureCallback photoCaptureCallback) {
        mPhotoCaptureCallback = photoCaptureCallback;
    }

    public File getSavedFile() {
        return mSavedFile;
    }

    public void setSavedFile(File savedFile) {
        mSavedFile = savedFile;
    }
}
