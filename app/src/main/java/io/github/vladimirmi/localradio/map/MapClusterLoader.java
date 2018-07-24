package io.github.vladimirmi.localradio.map;

import android.arch.persistence.db.SupportSQLiteQuery;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 18.07.2018.
 */
public class MapClusterLoader {

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

    private final Observable<Object> cameraObservable = observeCameraMove()
            .share();

    private final Observable<SupportSQLiteQuery> zoomObservable = cameraObservable
            .map(o -> map.getCameraPosition().zoom)
            .distinctUntilChanged()
            .doOnNext(zoom -> loadNext())
            .ignoreElements()
            .toObservable();

    private final Observable<SupportSQLiteQuery> targetObservable = cameraObservable
            .map(o -> map.getCameraPosition().target)
            .distinctUntilChanged()
            .doOnNext(this::checkThreshold)
            .ignoreElements()
            .toObservable();

    public MapClusterLoader(GoogleMap map, ClusterManager<LocationClusterItem> clusterManager) {
        this.map = map;
        this.clusterManager = clusterManager;
        configureMap();
    }

    public void addClusters(List<LocationClusterItem> clusters) {
        Timber.e("addClusters: " + clusters.size());
        clusterManager.addItems(clusters);
        clusterManager.cluster();
    }

    public Observable<SupportSQLiteQuery> getQueryObservable() {
        return Observable.merge(loadQueryRelay, zoomObservable, targetObservable)
                .observeOn(Schedulers.io());
    }

    public Observable<Object> getCameraObservable() {
        return cameraObservable;
    }

    public void setIsCountry(boolean isCountry) {
        boolean old = this.isCountry;
        this.isCountry = isCountry;
        if (old != isCountry) {
            loadBounds = null;
            loadNext();
        }
    }

    private void configureMap() {
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);

        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
    }

    private void checkThreshold(LatLng target) {
        if (originTarget == null) return;
        double deltaLat = MapUtils.delta(originTarget.latitude, target.latitude);
        if (deltaLat >= thresholdLat) {
            loadNext();
            return;
        }
        double deltaLong = MapUtils.delta(originTarget.longitude, target.longitude);
        if (deltaLong >= thresholdLong) {
            loadNext();
        }
    }

    private void loadNext() {
        calculateBounds();
        Bounds newLoadBounds = visibleBounds.multiplyBy(LOAD);
        if (newLoadBounds.equals(loadBounds)) return;

        if (loadBounds == null) {
            clusterManager.clearItems();
            clusterManager.cluster();
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

    private final Object emit = new Object();

    private Observable<Object> observeCameraMove() {
        return Observable.create(emitter -> {
            GoogleMap.OnCameraMoveListener listener = () -> {
                try {
                    if (!emitter.isDisposed()) emitter.onNext(emit);
                } catch (Exception e) {
                    emitter.tryOnError(e);
                }
            };
            map.setOnCameraMoveListener(listener);
            emitter.onNext(emit);

            emitter.setDisposable(Disposables.fromRunnable(() -> {
                map.setOnCameraMoveListener(null);
            }));
        }).sample(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread(), true)
                .unsubscribeOn(AndroidSchedulers.mainThread());
    }
}
