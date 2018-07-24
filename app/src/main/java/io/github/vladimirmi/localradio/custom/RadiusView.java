package io.github.vladimirmi.localradio.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 23.07.2018.
 */

public class RadiusView extends View {

    private static final double BASE_RADIUS_DP = 50.0 * 256 / 24095;
    private double baseRadius;
    private double zoom;
    private Paint paint;

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
        baseRadius = UiUtils.dpToPx(getContext(), BASE_RADIUS_DP);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.radius));
    }

    public void setZoomLevel(float zoom) {
        this.zoom = zoom;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Timber.e("onDraw: ");
        float radius = (float) (baseRadius * Math.pow(2, zoom));
        canvas.drawCircle(getWidth() / 2, canvas.getHeight() / 2, radius, paint);
    }
}
