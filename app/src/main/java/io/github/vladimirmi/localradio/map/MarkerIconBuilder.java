package io.github.vladimirmi.localradio.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 12.07.2018.
 */
public class MarkerIconBuilder {

    private static final int textM = 14; //sp
    private static final int markerS = 32; //dp
    private static final int markerM = 38; //dp
    private static final int markerL = 42; //dp
    private static final int markerXL = 48; //dp
    private static final int markerXXL = 56; //dp

    private final Context context;

    private final Paint textPaint = new Paint();
    private final Paint circlePaint = new Paint();

    private int textColor;
    private int textSize;
    private int markerColor;
    private int markerSize;
    private String text;
    private int stations;
    private boolean isCluster;
    private boolean isSelected;

    public MarkerIconBuilder(Context context) {
        this.context = context;

        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);

        textColor = Color.WHITE;
        textSize = UiUtils.spToPx(context, textM);
    }

    public MarkerIconBuilder stations(int stations) {
        this.stations = stations;
        return this;
    }

    public MarkerIconBuilder isCluster() {
        isCluster = true;
        return this;
    }

    public MarkerIconBuilder setSelected(boolean isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    public BitmapDescriptor build() {
        updateText();
        if (isCluster) {
            updateClusterColor();
            updateClusterSize();
        } else {
            updateMarkerSize();
            markerColor = context.getResources().getColor(isSelected ? R.color.selected_marker
                    : R.color.colorAccent);
        }
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        circlePaint.setColor(markerColor);

        return BitmapDescriptorFactory.fromBitmap(createMarkerBitmap());
    }

    private Bitmap createMarkerBitmap() {
        float half = markerSize / 2f;

        float baseLine = half - (textPaint.ascent() + textPaint.descent()) / 2;

        Bitmap image = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(image);

        canvas.drawCircle(half, half, half, circlePaint);
        canvas.drawText(text, half, baseLine, textPaint);

        return image;
    }

    private void updateText() {
        if (isCluster) {
            if (stations < 100) {
                text = String.valueOf(stations);
            } else if (stations < 300) {
                text = "100+";
            } else if (stations < 500) {
                text = "300+";
            } else if (stations < 1000) {
                text = "500+";
            } else if (stations < 5000) {
                text = "1000+";
            } else {
                text = "5000+";
            }
        } else {
            text = String.valueOf(stations);
        }
    }

    private void updateMarkerSize() {
        int size;
        if (stations < 100) {
            size = markerS;
        } else if (stations < 1000) {
            size = markerM;
        } else if (stations < 10000) {
            size = markerL;
        } else {
            size = markerXL;
        }
        markerSize = UiUtils.dpToPx(context, size);
    }

    private void updateClusterSize() {
        int size;
        if (stations < 100) {
            size = markerL;
        } else if (stations < 1000) {
            size = markerXL;
        } else {
            size = markerXXL;
        }
        markerSize = UiUtils.dpToPx(context, size);
    }

    private void updateClusterColor() {
        int res;
        if (stations < 100) {
            res = R.color.cluster_1;
        } else if (stations < 500) {
            res = R.color.cluster_2;
        } else if (stations < 1000) {
            res = R.color.cluster_3;
        } else if (stations < 5000) {
            res = R.color.cluster_4;
        } else {
            res = R.color.cluster_5;
        }
        markerColor = context.getResources().getColor(res);
    }
}
