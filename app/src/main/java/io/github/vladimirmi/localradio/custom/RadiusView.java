package io.github.vladimirmi.localradio.custom;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.model.CameraPosition;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 23.07.2018.
 */

public class RadiusView extends View {

    // 50 miles in dp at equator
    // equator 24095 miles
    private static final double BASE_RADIUS_DP = 50.0 * 256 / 24095;
    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float radius;

    public RadiusView(Context context) {
        super(context);
        init();
    }

    public RadiusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadiusView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint.setColor(getResources().getColor(R.color.map_radius));

        borderPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        borderPaint.setStrokeWidth(5);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    @Keep
    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }

    public void setCameraPosition(CameraPosition position) {
        double radiusAtLatitude = BASE_RADIUS_DP / Math.cos(Math.toRadians(position.target.latitude))
                * Math.pow(2, position.zoom);
        float newRadius = (float) UiUtils.dpToPx(getContext(), radiusAtLatitude);
        float delta = Math.abs(radius - newRadius);
        if (delta < 5) return;
        long time = (long) delta;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "radius", this.radius, newRadius);
        animator.setDuration(time);
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, canvas.getHeight() / 2, radius, circlePaint);
        canvas.drawCircle(getWidth() / 2, canvas.getHeight() / 2, radius, borderPaint);
    }
}
