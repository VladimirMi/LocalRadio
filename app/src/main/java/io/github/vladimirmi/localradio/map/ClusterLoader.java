package io.github.vladimirmi.localradio.map;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.os.Handler;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.Collection;
import java.util.List;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 18.07.2018.
 */
public class ClusterLoader {

    private static final double LOAD = 0.5;
    private static final double THRESHOLD = 0.3;

    private final BehaviorRelay<SupportSQLiteQuery> loadQueryRelay = BehaviorRelay.create();

    private GoogleMap map;
    private ClusterManager<LocationClusterItem> clusterManager;

    private LatLng originTarget;
    private Bounds visibleBounds;
    private Bounds loadBounds;
    private double thresholdLat;
    private double thresholdLong;

    private boolean isCountry = false;

    public ClusterLoader(GoogleMap map, ClusterManager<LocationClusterItem> clusterManager) {
        this.map = map;
        this.clusterManager = clusterManager;
    }

    public void addClusters(List<LocationClusterItem> clusters) {
        Timber.e("addClusters: " + clusters.size());
        clusterManager.addItems(clusters);
        clusterManager.cluster();
    }

    public Observable<SupportSQLiteQuery> getQueryObservable(Observable<Object> cameraObservable) {
        return Observable.merge(loadQueryRelay,
                createZoomObservable(cameraObservable),
                createTargetObservable(cameraObservable))
                .observeOn(Schedulers.io());
    }

    public void setIsCountry(boolean isCountry) {
        boolean old = this.isCountry;
        this.isCountry = isCountry;
        if (old != isCountry) {
            clusterManager.clearItems();
            clusterManager.cluster();
            loadBounds = null;
            new Handler().postDelayed(this::loadNext, 500);
        }
    }

    public void onCameraIdle() {
        clusterManager.onCameraIdle();
    }

    private boolean isNeedLoad(LatLng target) {
        if (originTarget == null) return false;
        double deltaLat = MapUtils.delta(originTarget.latitude, target.latitude);
        double deltaLong = MapUtils.delta(originTarget.longitude, target.longitude);
        return deltaLat >= thresholdLat || deltaLong >= thresholdLong;
    }

    private void loadNext() {
        calculateBounds();
        Bounds newLoadBounds = visibleBounds.multiplyBy(LOAD);
        if (newLoadBounds.equals(loadBounds)) return;

        if (loadBounds == null) {
            loadQueryRelay.accept(MapUtils.createQueryFor(newLoadBounds, isCountry));

        } else {
            removeClustersOutside(newLoadBounds);
            List<Bounds> load = newLoadBounds.except(loadBounds);
            for (Bounds bounds : load) {
                loadQueryRelay.accept(MapUtils.createQueryFor(bounds, isCountry));
            }
        }
        loadBounds = newLoadBounds;
    }

    private void calculateBounds() {
        originTarget = map.getCameraPosition().target;

        visibleBounds = new Bounds(map.getProjection().getVisibleRegion().latLngBounds);

        thresholdLat = visibleBounds.height * THRESHOLD;
        thresholdLong = visibleBounds.width * THRESHOLD;
    }

    private void removeClustersOutside(Bounds bounds) {
        Collection<LocationClusterItem> clusters = clusterManager.getAlgorithm().getItems();
        for (LocationClusterItem cluster : clusters) {
            if (!bounds.contains(cluster.getPosition())) {
                clusterManager.removeItem(cluster);
            }
        }
        clusterManager.cluster();
    }

    private Observable<SupportSQLiteQuery> createTargetObservable(Observable<Object> cameraObservable) {
        return cameraObservable
                .map(o -> map.getCameraPosition().target)
                .distinctUntilChanged()
                .filter(this::isNeedLoad)
                .doOnNext(latLng -> loadNext())
                .ignoreElements()
                .toObservable();
    }

    private Observable<SupportSQLiteQuery> createZoomObservable(Observable<Object> cameraObservable) {
        return cameraObservable
                .map(o -> map.getCameraPosition().zoom)
                .distinctUntilChanged()
                .doOnNext(zoom -> loadNext())
                .ignoreElements()
                .toObservable();
    }
}