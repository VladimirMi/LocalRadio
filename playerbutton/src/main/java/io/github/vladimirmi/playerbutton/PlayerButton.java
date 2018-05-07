package io.github.vladimirmi.playerbutton;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;


public class PlayerButton extends android.support.v7.widget.AppCompatImageButton {

    private boolean isPlaying = false;
    private boolean isManualMode = false;
    private OnClickListener listener;

    public PlayerButton(Context context) {
        this(context, null);
    }

    public PlayerButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDrawable();

        super.setOnClickListener(v -> {
            if (listener != null) listener.onClick(v);
            if (!isManualMode) setPlaying(!isPlaying);
        });
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        listener = l;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Set playing mode. Animate if needed
     *
     * @param isPlaying {@code true} - playing mode, {@code false} - paused mode
     */
    public void setPlaying(boolean isPlaying) {
        if (this.isPlaying != isPlaying) {
            setDrawable();
            startAnimation();
        }
        this.isPlaying = isPlaying;
    }

    /**
     * Set manual mode
     * <p>In manual mode, you must to call {@link #setPlaying(boolean)} to change the button state.
     * Otherwise, the button will also change the state itself on click.</p>
     */
    public void setManualMode(boolean isManualMode) {
        this.isManualMode = isManualMode;
    }

    private void setDrawable() {
        Drawable drawable = ContextCompat.getDrawable(getContext(),
                isPlaying ? R.drawable.pause_to_play_animation : R.drawable.play_to_pause_animation);
        setImageDrawable(drawable);
    }

    private void startAnimation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ((AnimatedVectorDrawableCompat) getDrawable()).start();
        } else {
            ((AnimatedVectorDrawable) getDrawable()).start();
        }
    }
}
