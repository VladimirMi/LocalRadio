package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;

/**
 * Created by Vladimir Mikhalev 12.07.2018.
 */
public class MapHelper {

    private static final int textSize = 14; //sp
    private static final int markerS = 32; //dp
    private static final int markerM = 38; //dp
    private static final int markerL = 42; //dp
    private static final int markerXL = 48; //dp

    private final Context context;

    private final Paint textPaint = new Paint();
    private final Paint circlePaint = new Paint();

    public MapHelper(Context context) {
        this.context = context;

        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);

        circlePaint.setColor(context.getResources().getColor(R.color.colorAccent));
    }

    public MarkerOptions createCountryMarker(LocationEntity entity) {
        return new MarkerOptions()
                .position(new LatLng(entity.latitude, entity.longitude))
                .title(entity.name)
                .anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromBitmap(createCountryMarkerBitmap(entity.stations)));
    }

    private Bitmap createCountryMarkerBitmap(int stations) {
        MarkerConfig config = getMarkerConfig(stations);

        textPaint.setTextSize(config.textSize);
        float half = config.size / 2f;

        float baseLine = half - (textPaint.ascent() + textPaint.descent()) / 2;

        Bitmap image = Bitmap.createBitmap(config.size, config.size, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(image);

        canvas.drawCircle(half, half, half, circlePaint);
        canvas.drawText(config.text, half, baseLine, textPaint);

        return image;
    }

    private MarkerConfig getMarkerConfig(int stations) {
        MarkerConfig config = new MarkerConfig();
        config.text = String.valueOf(stations);
        config.textSize = UiUtils.spToPx(context, textSize);
        if (stations < 100) {
            config.size = UiUtils.dpToPx(context, markerS);

        } else if (stations < 1000) {
            config.size = UiUtils.dpToPx(context, markerM);

        } else if (stations < 10000) {
            config.size = UiUtils.dpToPx(context, markerL);

        } else {
            config.size = UiUtils.dpToPx(context, markerXL);
        }
        return config;
    }

    private static class MarkerConfig {

        public String text;
        public int size;
        public int textSize;
    }
}
