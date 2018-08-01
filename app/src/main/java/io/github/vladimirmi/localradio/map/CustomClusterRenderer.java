package io.github.vladimirmi.localradio.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.Collections;
import java.util.Set;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;

/**
 * Created by Vladimir Mikhalev 12.07.2018.
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<LocationClusterItem> {

    private final Context context;
    private Set<LocationClusterItem> selectedItems = Collections.emptySet();

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
                        .setSelected(selectedItems.contains(item))
                        .build()
                );
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<LocationClusterItem> cluster, MarkerOptions markerOptions) {
        markerOptions
                .position(cluster.getPosition())
                .anchor(0.5f, 0.5f)
                .icon(new MarkerIconBuilder(context)
                        .isCluster()
                        .setSelected(isClusterSelected(cluster))
                        .stations(calculateStations(cluster))
                        .build()
                );
    }

    @Override
    protected void onClusterItemRendered(LocationClusterItem clusterItem, Marker marker) {
        marker.setIcon(new MarkerIconBuilder(context)
                .stations(clusterItem.getStationsNum())
                .setSelected(selectedItems.contains(clusterItem))
                .build());
    }

    @Override
    protected void onClusterRendered(Cluster<LocationClusterItem> cluster, Marker marker) {
        marker.setIcon(new MarkerIconBuilder(context)
                .stations(calculateStations(cluster))
                .isCluster()
                .setSelected(isClusterSelected(cluster))
                .build());
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<LocationClusterItem> cluster) {
        return cluster.getSize() > 2;
    }

    public void selectItems(Set<LocationClusterItem> items) {
        for (LocationClusterItem item : selectedItems) {
            Marker marker = getMarker(item);
            if (marker != null && !items.contains(item)) {
                marker.setIcon(new MarkerIconBuilder(context)
                        .stations(item.getStationsNum())
                        .setSelected(false)
                        .build());
            }
        }
        selectedItems = items;

        for (LocationClusterItem item : selectedItems) {
            Marker marker = getMarker(item);
            if (marker != null) {
                marker.setIcon(new MarkerIconBuilder(context)
                        .stations(item.getStationsNum())
                        .setSelected(true)
                        .build());
            }
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
}
