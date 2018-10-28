package io.github.vladimirmi.localradio.map;

import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.vladimirmi.localradio.R;
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
    private final Set<LocationClusterItem> emptyLocations = Collections.emptySet();
    private final Observable<Object> cameraMoveObservable;
    private final CustomClusterManager clusterManager;
    private final Context context;
    private final GoogleMap map;

    private OnSaveMapPositionListener onSavePositionListener;
    private String mapMode = COUNTRY_MODE;
    private PublishRelay<Set<LocationClusterItem>> selection = PublishRelay.create();
    private boolean needReCluster;
    private boolean modeChanged;


    public MapWrapper(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;

        configureMap();
        clusterManager = new CustomClusterManager(context, map);
        cameraMoveObservable = createCameraMoveObservable()
                .doOnNext(o -> clusterManager.cluster());

        map.setOnCameraIdleListener(this);
        map.setOnMarkerClickListener(clusterManager);

        clusterManager.setOnClusterItemClickListener(locationClusterItem -> {
            if (!mapMode.equals(RADIUS_MODE)) {
                selection.accept(Collections.singleton(locationClusterItem));
            }
            return false;
        });
    }

    public GoogleMap getMap() {
        return map;
    }


    public void addClusters(Set<LocationClusterItem> clusterItems) {
        clusterManager.addItems(clusterItems);
        if (needReCluster) clusterManager.cluster();
        if (mapMode.equals(RADIUS_MODE)) {
            selectInsideRadius();
        }
    }

    public Observable<CameraPosition> getRadiusChangeObservable() {
        return cameraMoveObservable
                .map(o -> map.getCameraPosition())
                .distinctUntilChanged()
                .filter(zoom -> mapMode.equals(RADIUS_MODE));
    }

    public Observable<Set<LocationClusterItem>> getSelectedItemsObservable() {
        Observable<Set<LocationClusterItem>> radiusSelection = cameraMoveObservable
                .filter(o -> mapMode.equals(RADIUS_MODE))
                .distinctUntilChanged(o -> map.getCameraPosition().target)
                .map(o -> MapUtils.insideRadiusMiles(clusterManager.getAlgorithm().getItems(),
                        map.getCameraPosition().target, 50));

        return Observable.merge(selection, radiusSelection);
    }

    @Override
    public void onCameraIdle() {
        if (onSavePositionListener != null) {
            float zoom = map.getCameraPosition().zoom;
            LatLng target = map.getCameraPosition().target;
            onSavePositionListener.onSaveMapPosition(new MapPosition(target, zoom));
        }
    }

    public void setOnSaveMapPositionListener(OnSaveMapPositionListener listener) {
        onSavePositionListener = listener;
    }

    public void restoreMapPosition(MapPosition state, boolean animate) {
        LatLng position = new LatLng(state.latitude, state.longitude);
        int zoom = mapMode.equals(COUNTRY_MODE) ? 5 : 7;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, zoom);
        if (animate) map.animateCamera(cameraUpdate);
        else map.moveCamera(cameraUpdate);

    }

    public void selectClusters(Set<LocationClusterItem> items) {
        clusterManager.selectClusters(items);
        if (modeChanged) clusterManager.cluster();
    }

    public void setMapMode(String mode) {
        needReCluster = mapMode.equals(COUNTRY_MODE) != mode.equals(COUNTRY_MODE);
        modeChanged = mapMode.equals(mode);
        if (needReCluster) clusterManager.clearItems();

        switch (mode) {
            case EXACT_MODE: {
                selection.accept(emptyLocations);
                map.animateCamera(CameraUpdateFactory.zoomTo(7));
                map.setMinZoomPreference(7f);
                map.setMaxZoomPreference(10f);
                break;
            }
            case RADIUS_MODE: {
                if (mapMode.equals(EXACT_MODE)) selectInsideRadius();
                map.animateCamera(CameraUpdateFactory.zoomTo(7));
                map.setMinZoomPreference(6f);
                map.setMaxZoomPreference(9f);
                break;
            }
            case COUNTRY_MODE: {
                selection.accept(emptyLocations);
                map.animateCamera(CameraUpdateFactory.zoomTo(5));
                map.setMinZoomPreference(2f);
                map.setMaxZoomPreference(6f);
                break;
            }
        }
        mapMode = mode;
    }

    private void selectInsideRadius() {
        selection.accept(MapUtils.insideRadiusMiles(clusterManager.getAlgorithm().getItems(),
                map.getCameraPosition().target, 50));
    }

    private void configureMap() {
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));
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

    public interface OnSaveMapPositionListener {

        void onSaveMapPosition(MapPosition position);
    }
}
