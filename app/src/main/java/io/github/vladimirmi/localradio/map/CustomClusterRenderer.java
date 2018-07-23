package io.github.vladimirmi.localradio.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import io.github.vladimirmi.localradio.domain.models.LocationCluster;

/**
 * Created by Vladimir Mikhalev 12.07.2018.
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<LocationCluster> {

    private final Context context;

    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<LocationCluster> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(LocationCluster item, MarkerOptions markerOptions) {
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
    protected void onBeforeClusterRendered(Cluster<LocationCluster> cluster, MarkerOptions markerOptions) {
        int stations = 0;
        for (LocationCluster locationCluster : cluster.getItems()) {
            stations += locationCluster.getStationsNum();
        }

        markerOptions
                .position(cluster.getPosition())
                .anchor(0.5f, 0.5f)
                .icon(new MarkerIconBuilder(context)
                        .isCluster()
                        .stations(stations)
                        .build()
                );
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<LocationCluster> cluster) {
        return cluster.getSize() > 2;
    }
}
