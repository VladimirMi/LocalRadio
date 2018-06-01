package io.github.vladimirmi.localradio.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class NonSwipeableViewPager extends ViewPager {

    public static final int ANIMATION_DURATION = 300;

    public NonSwipeableViewPager(@NonNull Context context) {
        this(context, null);
    }

    public NonSwipeableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setSmoothScroller();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    private void setSmoothScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            scroller.set(this, new SmoothScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SmoothScroller extends Scroller {

        SmoothScroller(Context context) {
            super(context, new DecelerateInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, ANIMATION_DURATION);
        }
    }
}
