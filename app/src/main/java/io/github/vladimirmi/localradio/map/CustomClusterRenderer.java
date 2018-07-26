package io.github.vladimirmi.localradio.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 12.07.2018.
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<LocationClusterItem> {

    private final Context context;
    private List<LocationClusterItem> selectedItems = Collections.emptyList();

    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<LocationClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(LocationClusterItem item, MarkerOptions markerOptions) {

        markerOptions
                .position(item.getPosition())
                .title(item.getTitle())
                .anchor(0.5f, 0.5f)
                .icon(new MarkerIconBuilder(context)
                        .stations(item.getStationsNum())
                        .build()
                );
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<LocationClusterItem> cluster, MarkerOptions markerOptions) {
        int stations = calculateStations(cluster);
        boolean isSelected = isClusterSelected(cluster);
        Timber.d("onBeforeClusterRendered: %s, %s", stations, isSelected);

        markerOptions
                .position(cluster.getPosition())
                .anchor(0.5f, 0.5f)
                .icon(new MarkerIconBuilder(context)
                        .isCluster()
                        .setSelected(isSelected)
                        .stations(stations)
                        .build()
                );
    }

    @Override
    protected void onClusterRendered(Cluster<LocationClusterItem> cluster, Marker marker) {
        int stations = 0;
        for (LocationClusterItem locationCluster : cluster.getItems()) {
            stations += locationCluster.getStationsNum();
        }
        Timber.i("onClusterRendered: " + stations);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<LocationClusterItem> cluster) {
        boolean containsAtLeastOne = false;
        boolean containsAll = true;
        for (LocationClusterItem clusterItem : cluster.getItems()) {
            if (selectedItems.contains(clusterItem)) {
                containsAtLeastOne = true;
            } else {
                containsAll = false;
            }
        }
        //noinspection SimplifiableIfStatement
        if (containsAtLeastOne && !containsAll) {
            return false;
        } else {
            return cluster.getSize() > 2;
        }
    }

    public void selectItems(List<LocationClusterItem> items) {
        for (LocationClusterItem item : except(selectedItems, items)) {
            Marker marker = getMarker(item);
            if (marker == null) continue;
            marker.setIcon(new MarkerIconBuilder(context)
                    .stations(item.getStationsNum())
                    .setSelected(false)
                    .build());
        }

        selectedItems = items;

        for (LocationClusterItem item : selectedItems) {
            Marker marker = getMarker(item);
            if (marker == null) continue;
            marker.setIcon(new MarkerIconBuilder(context)
                    .stations(item.getStationsNum())
                    .setSelected(true)
                    .build());


            Cluster<LocationClusterItem> cluster = getCluster(marker);
            if (cluster == null) continue;
            Marker clusterMarker = getMarker(cluster);
            if (clusterMarker == null) continue;

            clusterMarker.setIcon(new MarkerIconBuilder(context)
                    .stations(calculateStations(cluster))
                    .isCluster()
                    .setSelected(isClusterSelected(cluster))
                    .build());
        }
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

    private List<LocationClusterItem> except(List<LocationClusterItem> list1,
                                             List<LocationClusterItem> list2) {

        List<LocationClusterItem> result = new ArrayList<>();
        for (LocationClusterItem item : list1) {
            if (!list2.contains(item)) result.add(item);
        }
        return result;
    }
}
