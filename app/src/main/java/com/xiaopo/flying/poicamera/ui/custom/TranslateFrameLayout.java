package com.xiaopo.flying.poicamera.ui.custom;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by snowbean on 16-6-2.
 */
public class TranslateFrameLayout extends FrameLayout {
    private View mTopView;
    private View mBottomView;
    private boolean mIsTopViewShowing;
    private boolean mIsBottomViewShowing;
    private boolean mIsBottomToTop;

    public TranslateFrameLayout(Context context) {
        super(context);
    }

    public TranslateFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TranslateFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() < 2) {
            throw new IllegalStateException("This layout must 2 child view");
        }

        mTopView = getChildAt(1);
        mBottomView = getChildAt(0);

    }

    public void setBottomToTop(boolean bottomToTop) {
        mIsBottomToTop = bottomToTop;
    }

    private void showTopView() {
        if (mTopView.getVisibility() == INVISIBLE) {
            mTopView.setTranslationY(-mTopView.getHeight());
//            mTopView.animate().translationY(-mTopView.getHeight()).setDuration(0).start();
            mTopView.setVisibility(VISIBLE);
        }
        int height;
        if (!mIsBottomToTop) {
            height = -mTopView.getHeight();
        } else {
            height = mTopView.getHeight();
        }
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(mTopView, "translationY", height, 0);
        translateAnim.setDuration(200).setStartDelay(50);
        translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnim.start();
    }

    private void dismissTopView() {
        int height;
        if (!mIsBottomToTop) {
            height = -mTopView.getHeight();
        } else {
            height = mTopView.getHeight();
        }
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(mTopView, "translationY", height);
        translateAnim.setDuration(200).setStartDelay(50);
        translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnim.start();
    }

    private void showBottomView() {
        if (mBottomView.getVisibility() == INVISIBLE) {
            mBottomView.setTranslationY(-mBottomView.getHeight());
//            mTopView.animate().translationY(-mTopView.getHeight()).setDuration(0).start();
            mBottomView.setVisibility(VISIBLE);
        }
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(mBottomView, "translationY", mBottomView.getHeight(), 0);
        translateAnim.setDuration(100).setStartDelay(50);
        translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnim.start();
    }

    private void dismissBottomView() {
        ObjectAnimator translateAnim = ObjectAnimator.ofFloat(mBottomView, "translationY", mBottomView.getHeight());
        translateAnim.setDuration(100).setStartDelay(50);
        translateAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateAnim.start();
    }


    public void dismissOrShowTopView() {
        if (mIsTopViewShowing) {
            dismissTop();
        } else {
            showTop();
        }
    }


    private void dismissTop() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissTopView();
                mIsTopViewShowing = false;
            }
        }, 200);
    }

    private void showTop() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                showTopView();
                mIsTopViewShowing = true;
            }
        }, 200);
    }


    public void dismissOrShowBottomView() {
        if (mIsBottomViewShowing) {
            dismissBottom();
        } else {
            showBottom();
        }
    }


    private void dismissBottom() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissBottomView();
                mIsBottomViewShowing = false;
            }
        }, 200);
    }

    private void showBottom() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                showBottomView();
                mIsBottomViewShowing = true;
            }
        }, 200);
    }
}
