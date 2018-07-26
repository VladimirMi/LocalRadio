package io.github.vladimirmi.localradio.map;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public class MapWrapper implements GoogleMap.OnCameraIdleListener {

    public static final String EXACT_MODE = "EXACT_MODE";
    public static final String RADIUS_MODE = "RADIUS_MODE";
    public static final String COUNTRY_MODE = "COUNTRY_MODE";

    public final GoogleMap map;
    private final Object emit = new Object();
    private final Observable<Object> cameraMoveObservable;
    private final CustomClusterRenderer renderer;
    private final ClusterManager<LocationClusterItem> clusterManager;
    private final ClusterLoader clusterLoader;
    private String mapMode = COUNTRY_MODE;
    private OnSaveMapStateListener onSaveStateListener;

    public MapWrapper(GoogleMap map, Context context) {
        this.map = map;
        cameraMoveObservable = createCameraMoveObservable();
        configureMap();

        clusterManager = new ClusterManager<>(context, map);
        renderer = new CustomClusterRenderer(context, map, clusterManager);
        clusterManager.setRenderer(renderer);
        map.setOnCameraIdleListener(this);

        clusterLoader = new ClusterLoader(map, clusterManager);
    }

    public void setMapMode(String mode) {
        mapMode = mode;
        clusterLoader.setIsCountry(mode.equals(COUNTRY_MODE));
    }

    public void addClusters(List<LocationClusterItem> clusterItems) {
        clusterLoader.addClusters(clusterItems);
    }

    public Observable<SupportSQLiteQuery> getQueryObservable() {
        return clusterLoader.getQueryObservable(cameraMoveObservable);
    }

    public Observable<Float> getRadiusZoomObservable() {
        return cameraMoveObservable
                .map(o -> map.getCameraPosition().zoom)
                .distinctUntilChanged()
                .filter(zoom -> mapMode.equals(RADIUS_MODE));
    }

    public void setOnSaveStateListener(OnSaveMapStateListener listener) {
        onSaveStateListener = listener;
    }

    public void restoreMapState(MapState state) {
        LatLng position = new LatLng(state.latitude, state.longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, state.zoom);
        map.moveCamera(cameraUpdate);
    }

    public Observable<List<LocationClusterItem>> getSelectedItemsObservable() {
        // TODO: 7/24/18 refactor collect from many sources
        return cameraMoveObservable
                .map(o -> clusterManager.getAlgorithm().getItems())
                .flatMapSingle(locationClusterItems -> Observable.fromIterable(locationClusterItems)
                        .filter(item -> MapUtils.distance(map.getCameraPosition().target, item.getPosition()) < 50)
                        .toList())
                .doOnNext(clusterItems -> {
                    renderer.selectItems(clusterItems);
                    clusterManager.cluster();
                });
    }

    @Override
    public void onCameraIdle() {
        clusterManager.onCameraIdle();
        float zoom = map.getCameraPosition().zoom;
        LatLng target = map.getCameraPosition().target;
        onSaveStateListener.onSaveMapState(new MapState(target, zoom));
    }

    private Observable<Object> createCameraMoveObservable() {
        return Observable.create(emitter -> {
            GoogleMap.OnCameraMoveListener cameraMoveListener = () -> {
                if (!emitter.isDisposed()) emitter.onNext(emit);
            };
            map.setOnCameraMoveListener(cameraMoveListener);
            emitter.onNext(emit);

            emitter.setDisposable(Disposables.fromRunnable(() -> map.setOnCameraMoveListener(null)));
        }).sample(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread(), true)
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .share();
    }

    private void configureMap() {
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
    }

    public interface OnSaveMapStateListener {

        void onSaveMapState(MapState state);
    }
}
