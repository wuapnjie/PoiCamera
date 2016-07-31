package com.xiaopo.flying.poicamera.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by snowbean on 16-7-22.
 */

public class ActivityUtil {
    private ActivityUtil() {

    }

    public static void addFragmentToActivity(FragmentManager fragmentManager, Fragment fragment, int frameId) {

        if (fragmentManager == null || fragment == null) {
            throw new NullPointerException();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

}
