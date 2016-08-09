package com.xiaopo.flying.poicamera.datatype;

import android.content.Context;

import com.xiaopo.flying.poicamera.filter.FilterType;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by snowbean on 16-8-2.
 */

public class Filter {
    private String name;
    private FilterType filterType;

    public Filter(String name, FilterType filterType) {
        this.name = name;
        this.filterType = filterType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public int getPreviewPicture() {
        if (filterType != null) {
            return filterType.obtainPreviewImage();
        }
        return android.R.color.transparent;
    }

    public GPUImageFilter getImageFilter(Context context) {
        if (filterType != null) {
            return filterType.obtainFilter(context);
        }
        return new GPUImageFilter();
    }
}
