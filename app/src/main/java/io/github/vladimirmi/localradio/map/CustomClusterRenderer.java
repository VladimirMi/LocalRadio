package io.github.vladimirmi.localradio.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Collections;
import java.util.Set;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 12.07.2018.
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<LocationClusterItem> {

    private static final int MARKER_SIZE_DP = 48;
    private static final int TEXT_SIZE_S = 8; //sp
    private static final int TEXT_SIZE_M = 10; //sp
    private static final int TEXT_SIZE_L = 12; //sp
    private static final int[] BUCKETS = {100, 200, 500, 1000, 5000};

    private final Context context;
    private Set<LocationClusterItem> selectedItems = Collections.emptySet();
    private final Paint textPaint = new Paint();
    private final Drawable marker;
    private final Drawable selectedMarker;
    private final int markerSize;


    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<LocationClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;

        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.WHITE);

        marker = ContextCompat.getDrawable(context, R.drawable.ic_location);
        selectedMarker = ContextCompat.getDrawable(context, R.drawable.ic_location_selected);
        markerSize = UiUtils.dpToPx(context, MARKER_SIZE_DP);
    }

    public void selectItems(Set<LocationClusterItem> items) {
        Set<LocationClusterItem> previousSelectedItems = selectedItems;
        selectedItems = items;
        for (LocationClusterItem item : previousSelectedItems) {
            Marker marker = getMarker(item);
            if (marker != null && !selectedItems.contains(item)) {
                marker.setIcon(createMarkerIcon(item));
            }
        }
        for (LocationClusterItem item : selectedItems) {
            Marker marker = getMarker(item);
            if (marker != null && !previousSelectedItems.contains(item)) {
                marker.setIcon(createMarkerIcon(item));
            }
        }
        this.selectedItems = items;
    }

    @Override
    protected void onBeforeClusterItemRendered(LocationClusterItem item, MarkerOptions markerOptions) {
        markerOptions.icon(createMarkerIcon(item));
    }

    @Override
    protected void onClusterItemRendered(LocationClusterItem clusterItem, Marker marker) {
        marker.setIcon(createMarkerIcon(clusterItem));
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<LocationClusterItem> cluster) {
        return cluster.getSize() > 2;
    }

    @Override
    protected int getBucket(Cluster<LocationClusterItem> cluster) {
        int size = calculateStations(cluster);
        int bucket = 0;
        if (size <= BUCKETS[0]) {
            bucket = size;
        }
        if (bucket == 0) {
            for (int i = 0; i < BUCKETS.length - 1; i++) {
                if (size < BUCKETS[i + 1]) {
                    bucket = BUCKETS[i];
                }
            }
        }
        if (bucket == 0) bucket = BUCKETS[BUCKETS.length - 1];
        return isClusterSelected(cluster) ? -bucket : bucket;
    }

    @Override
    protected int getColor(int clusterSize) {
        return clusterSize < 0 ? ContextCompat.getColor(context, R.color.selected_marker)
                : super.getColor(clusterSize);
    }

    @Override
    protected String getClusterText(int bucket) {
        bucket = Math.abs(bucket);
        return bucket < BUCKETS[0] ? String.valueOf(bucket) : bucket + "+";
    }

    private boolean isClusterSelected(Cluster<LocationClusterItem> cluster) {
        boolean isSelected = true;
        for (LocationClusterItem item : cluster.getItems()) {
            if (!selectedItems.contains(item)) {
                isSelected = false;
                break;
            }
        }
        return isSelected;
    }

    private int calculateStations(Cluster<LocationClusterItem> cluster) {
        int stations = 0;
        for (LocationClusterItem item : cluster.getItems()) {
            stations += item.getStationsNum();
        }
        return stations;
    }

    private BitmapDescriptor createMarkerIcon(LocationClusterItem item) {
        float half = markerSize / 2f;
        float baseLine = half * 0.8f - (textPaint.ascent() + textPaint.descent()) / 2;
        String text = getMarkerText(item);

        Bitmap image = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(image);

        Drawable drawable = selectedItems.contains(item) ? selectedMarker : marker;
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        canvas.drawText(text, half, baseLine, textPaint);

        return BitmapDescriptorFactory.fromBitmap(image);
    }

    private String getMarkerText(LocationClusterItem item) {
        int stations = item.getStationsNum();
        if (stations < 1000) {
            textPaint.setTextSize(UiUtils.spToPx(context, TEXT_SIZE_L));
        } else if (stations < 10000) {
            textPaint.setTextSize(UiUtils.spToPx(context, TEXT_SIZE_M));
        } else {
            textPaint.setTextSize(UiUtils.spToPx(context, TEXT_SIZE_S));
        }
        return String.valueOf(stations);
    }
}
