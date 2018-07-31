package io.github.vladimirmi.localradio.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
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
    private float radius;
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
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.map_radius));
    }

    public void setCameraPosition(CameraPosition position) {
        double radiusAtLatitude = BASE_RADIUS_DP / Math.cos(Math.toRadians(position.target.latitude))
                * Math.pow(2, position.zoom);
        radius = (float) UiUtils.dpToPx(getContext(), radiusAtLatitude);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getWidth() / 2, canvas.getHeight() / 2, radius, paint);
    }
}
