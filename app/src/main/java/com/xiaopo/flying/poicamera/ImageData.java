package com.xiaopo.flying.poicamera;

import android.media.Image;

import java.io.File;

/**
 * Created by snowbean on 16-7-31.
 */

public class ImageData {
    private final Image mImage;
    private final File mFile;

    public ImageData(Image image, File file) {
        mImage = image;
        mFile = file;
    }

    public Image getImage() {
        return mImage;
    }

    public File getFile() {
        return mFile;
    }

}
