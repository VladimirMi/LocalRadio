package io.github.vladimirmi.localradio.map;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;

/**
 * Created by Vladimir Mikhalev 31.07.2018.
 */
public class MapWrapper implements GoogleMap.OnCameraIdleListener {

    public static final String EXACT_MODE = "EXACT_MODE";
    public static final String RADIUS_MODE = "RADIUS_MODE";
    public static final String COUNTRY_MODE = "COUNTRY_MODE";

    private final Object emit = new Object();
    private final Observable<Object> cameraMoveObservable;
    private final ClusterLoader clusterLoader;
    private final CustomClusterManager clusterManager;

    private OnSaveMapStateListener onSaveStateListener;
    private String mapMode = COUNTRY_MODE;
    private PublishRelay<Set<LocationClusterItem>> manualSelection = PublishRelay.create();

    private final GoogleMap map;

    public MapWrapper(Context context, GoogleMap map) {
        this.map = map;

        cameraMoveObservable = createCameraMoveObservable();
        configureMap();
        clusterManager = new CustomClusterManager(context, map);
        clusterLoader = new ClusterLoader(map, clusterManager);

        map.setOnCameraIdleListener(this);
        map.setOnMarkerClickListener(clusterManager);


        clusterManager.setOnClusterItemClickListener(locationClusterItem -> {
            if (!mapMode.equals(RADIUS_MODE)) {
                manualSelection.accept(Collections.singleton(locationClusterItem));
            }
            return false;
        });
    }

    public GoogleMap getMap() {
        return map;
    }

    private boolean firstAdd = true;

    public void addClusters(List<LocationClusterItem> clusterItems) {
        clusterManager.addItems(clusterItems);
        if (firstAdd && RADIUS_MODE.equals(mapMode)) {
            manualSelection.accept(selectClustersInsideRadius());
            firstAdd = false;
        } else {
            clusterManager.cluster();
        }
    }

    public Observable<SupportSQLiteQuery> getQueryObservable() {
        return clusterLoader.getQueryObservable(cameraMoveObservable);
    }

    public Observable<CameraPosition> getRadiusChangeObservable() {
        return cameraMoveObservable
                .map(o -> map.getCameraPosition())
                .distinctUntilChanged()
                .startWith(map.getCameraPosition())
                .filter(zoom -> mapMode.equals(RADIUS_MODE));
    }

    public Observable<Set<LocationClusterItem>> getSelectedItemsObservable() {
        Observable<Set<LocationClusterItem>> radiusSelection = cameraMoveObservable
                .filter(o -> mapMode.equals(RADIUS_MODE))
                .map(o -> selectClustersInsideRadius());

        return Observable.merge(manualSelection, radiusSelection);
    }

    private Set<LocationClusterItem> selectClustersInsideRadius() {
        Set<LocationClusterItem> items = MapUtils.insideRadiusMiles(clusterManager.getAlgorithm().getItems(),
                map.getCameraPosition().target, 50);
        return clusterManager.selectClusters(items);
    }

    @Override
    public void onCameraIdle() {
        float zoom = map.getCameraPosition().zoom;
        LatLng target = map.getCameraPosition().target;
        onSaveStateListener.onSaveMapState(new MapState(target, zoom));
    }

    public void setOnSaveStateListener(OnSaveMapStateListener listener) {
        onSaveStateListener = listener;
    }

    public void restoreMapState(MapState state) {
        LatLng position = new LatLng(state.latitude, state.longitude);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, state.zoom);
        map.moveCamera(cameraUpdate);
    }

    public void setMapMode(String mode) {
        mapMode = mode;
        clusterLoader.setIsCountry(mode.equals(COUNTRY_MODE));

        switch (mode) {
            case EXACT_MODE: {
                manualSelection.accept(clusterManager.selectClusters(Collections.emptySet()));
                map.setMinZoomPreference(6f);
                map.setMaxZoomPreference(10f);
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(7);
                map.animateCamera(cameraUpdate);
                break;
            }
            case RADIUS_MODE: {
                if (firstAdd) manualSelection.accept(selectClustersInsideRadius());
                clusterManager.map.setMinZoomPreference(6f);
                clusterManager.map.setMaxZoomPreference(9f);
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(7);
                map.animateCamera(cameraUpdate);
                break;
            }
            case COUNTRY_MODE: {
                manualSelection.accept(clusterManager.selectClusters(Collections.emptySet()));
                clusterManager.map.setMinZoomPreference(2f);
                clusterManager.map.setMaxZoomPreference(6f);
                CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(5);
                map.animateCamera(cameraUpdate);
                break;
            }
        }
        firstAdd = true;
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

    private Observable<Object> createCameraMoveObservable() {
        return Observable.create(emitter -> {
            GoogleMap.OnCameraMoveListener cameraMoveListener = () -> {
                if (!emitter.isDisposed()) emitter.onNext(emit);
            };
            map.setOnCameraMoveListener(cameraMoveListener);

            emitter.setDisposable(Disposables.fromRunnable(() -> map.setOnCameraMoveListener(null)));
        }).sample(500, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread(), true)
                .unsubscribeOn(AndroidSchedulers.mainThread())
                .share();
    }

    public interface OnSaveMapStateListener {

        void onSaveMapState(MapState state);
    }
}
