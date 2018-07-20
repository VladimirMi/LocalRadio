package io.github.vladimirmi.localradio.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 18.07.2018.
 */
public class MapClusterLoader {

    private static final double LOAD = 0.5;
    private static final double THRESHOLD = 0.3;

    private final PublishRelay<String> loadQueryRelay = PublishRelay.create();

    private GoogleMap map;
    private ClusterManager<LocationCluster> clusterManager;

    private LatLng originTarget;
    private Bounds visibleBounds;
    private Bounds loadBounds;
    private double thresholdY;
    private double thresholdX;

    private boolean isCountry = false;
    private volatile boolean loading = false;

    private final Observable<String> zoomObservable = observeCameraMove(googleMap ->
            googleMap.getCameraPosition().zoom)
            .distinctUntilChanged()
            .doOnNext(zoom -> calculateBounds())
            .ignoreElements()
            .<String>toObservable()
            .doOnDispose(() -> Timber.e("dispose target"));

    private final Observable<String> targetObservable = observeCameraMove(googleMap ->
            googleMap.getCameraPosition().target)
            .distinctUntilChanged()
            .filter(latLng -> !loading)
            .doOnNext(this::checkThreshold)
            .ignoreElements()
            .<String>toObservable()
            .doOnDispose(() -> Timber.e("dispose target"));

    public MapClusterLoader(GoogleMap map, ClusterManager<LocationCluster> clusterManager) {
        this.map = map;
        this.clusterManager = clusterManager;
        configureMap();
    }

    public void addClusters(List<LocationCluster> clusters) {
        loading = false;
        clusterManager.addItems(clusters);
    }

    public Observable<String> observeQueryString() {
        return Observable.merge(zoomObservable, targetObservable, loadQueryRelay)
                .doOnSubscribe(disposable -> loadNext());
    }

    public void setIsCountry(boolean isCountry) {
        this.isCountry = isCountry;
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
        double deltaY = MapUtils.delta(originTarget.latitude, target.latitude);
        if (deltaY >= thresholdY) {
            loadNext();
            return;
        }
        double deltaX = MapUtils.delta(originTarget.longitude, target.longitude);
        if (deltaX >= thresholdX) {
            loadNext();
        }
    }

    private void loadNext() {
        calculateBounds();
        Bounds newLoadBounds = visibleBounds.multiplyBy(LOAD);

        if (loadBounds == null) {
            loadQueryRelay.accept(getQueryString(newLoadBounds));
            loading = true;

        } else {
            removeClustersOutside(newLoadBounds);
            List<Bounds> load = newLoadBounds.except(loadBounds);
            for (Bounds bounds : load) {
                loadQueryRelay.accept(getQueryString(bounds));
            }
        }
        loadBounds = newLoadBounds;
    }

    private void calculateBounds() {
        originTarget = map.getCameraPosition().target;

        visibleBounds = MapUtils.normalize(map.getProjection().getVisibleRegion().latLngBounds);

        thresholdY = visibleBounds.height * THRESHOLD;
        thresholdX = visibleBounds.width * THRESHOLD;
    }

    private void removeClustersOutside(Bounds bounds) {
        Collection<LocationCluster> clusters = clusterManager.getAlgorithm().getItems();
        for (LocationCluster cluster : clusters) {
            Point point = MapUtils.normalize(cluster.getPosition());
            if (!bounds.contains(point)) {
                clusterManager.removeItem(cluster);
            }
        }
    }

    private String getQueryString(Bounds bounds) {
        return new MapQueryBuilder()
                .isCountry(isCountry)
                .insideBounds(bounds)
                .build();
    }

    private <T> Observable<T> observeCameraMove(Function<GoogleMap, T> fun) {
        return Observable.create((ObservableOnSubscribe<T>) emitter -> {
            GoogleMap.OnCameraMoveListener listener = () -> {
                try {
                    if (!emitter.isDisposed()) emitter.onNext(fun.apply(map));
                } catch (Exception e) {
                    emitter.tryOnError(e);
                }
            };
            map.setOnCameraMoveListener(listener);
            emitter.onNext(fun.apply(map));

            emitter.setDisposable(Disposables.fromRunnable(() -> {
                map.setOnCameraMoveListener(null);
            }));
        }).sample(100, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread(), true);
    }
}
