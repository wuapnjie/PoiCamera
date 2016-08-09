package com.xiaopo.flying.poicamera.filter;

import android.content.Context;

import com.xiaopo.flying.poicamera.R;
import com.xiaopo.flying.poicamera.datatype.Filter;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by snowbean on 16-8-2.
 */

public enum FilterType {
    Normal("Normal"),
    F1977("F1997"),
    Amaro("Amaro"),
    Brannan("Brannan"),
    Earlybird("Earlybird"),
    Hefe("Hefe"),
    Hudson("Hudson"),
    Inkwell("Inkwell"),
    Lomo("Lomo"),
    Lord("Lord"),
    Nashviller("Nashviller"),
    Rise("Rise"),
    Sierra("Sierra"),
    Sutro("Sutro"),
    Toaster("Toaster"),
    Valencial("Valencial"),
    Walden("Walden"),
    XproII("XproII");


    private String mFilterType;

    FilterType(String filterType) {
        mFilterType = filterType;
    }

    public static FilterType buildFilterType(String str) {
        if (str != null) {
            for (FilterType n : FilterType.values()) {
                if (str.equalsIgnoreCase(n.mFilterType)) {
                    return n;
                }
            }
        }
        return FilterType.Normal;
    }

    public static List<Filter> obtainAllFilter(Context context) {
        List<Filter> filters = new ArrayList<>();
        for (FilterType n : FilterType.values()) {
            filters.add(new Filter(n.name(), n));
        }
        return filters;
    }

    public GPUImageFilter obtainFilter(Context context) {
        switch (this) {
            case Normal:
                return new IFNormalFilter(context);
            case F1977:
                return new IF1977Filter(context);
            case Amaro:
                return new IFAmaroFilter(context);
            case Brannan:
                return new IFBrannanFilter(context);
            case Earlybird:
                return new IFEarlybirdFilter(context);
            case Hefe:
                return new IFHefeFilter(context);
            case Hudson:
                return new IFHudsonFilter(context);
            case Inkwell:
                return new IFInkwellFilter(context);
            case Lomo:
                return new IFLomofiFilter(context);
            case Lord:
                return new IFLordKelvinFilter(context);
            case Nashviller:
                return new IFNashvilleFilter(context);
            case Rise:
                return new IFRiseFilter(context);
            case Sierra:
                return new IFSierraFilter(context);
            case Sutro:
                return new IFSutroFilter(context);
            case Toaster:
                return new IFToasterFilter(context);
            case Valencial:
                return new IFValenciaFilter(context);
            case Walden:
                return new IFWaldenFilter(context);
            case XproII:
                return new IFWaldenFilter(context);
            default:
                return new IFNormalFilter(context);
        }
    }

    public int obtainPreviewImage() {
        switch (this) {
            case Normal:
                return R.drawable.if_normal;
            case F1977:
                return R.drawable.if_1977;
            case Amaro:
                return R.drawable.if_amaro;
            case Brannan:
                return R.drawable.if_brannan;
            case Earlybird:
                return R.drawable.if_earlybird;
            case Hefe:
                return R.drawable.if_hefe;
            case Hudson:
                return R.drawable.if_hudson;
            case Inkwell:
                return R.drawable.if_inkwell;
            case Lomo:
                return R.drawable.if_lomofi;
            case Lord:
                return R.drawable.if_lordkelvin;
            case Nashviller:
                return R.drawable.if_nashville;
            case Rise:
                return R.drawable.if_rise;
            case Sierra:
                return R.drawable.if_sierra;
            case Sutro:
                return R.drawable.if_sutro;
            case Toaster:
                return R.drawable.if_toaster;
            case Valencial:
                return R.drawable.if_valencia;
            case Walden:
                return R.drawable.if_walden;
            case XproII:
                return R.drawable.if_xproii;
            default:
                return R.drawable.if_normal;
        }
    }
}
