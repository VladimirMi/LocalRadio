package io.github.vladimirmi.localradio.map;

import android.annotation.SuppressLint;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.view.ClusterRenderer;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public class CustomClusterManager extends ClusterManager<LocationClusterItem> {

    public static final String EXACT_MODE = "EXACT_MODE";
    public static final String RADIUS_MODE = "RADIUS_MODE";
    public static final String COUNTRY_MODE = "COUNTRY_MODE";

    private final Object emit = new Object();
    private final Observable<Object> cameraMoveObservable;
    private final ClusterLoader clusterLoader;

    private final CustomAlgorithm algorithm;
    private final ReadWriteLock algorithmLock = new ReentrantReadWriteLock();
    private final CustomClusterRenderer renderer;

    public final GoogleMap map;
    private final ReadWriteLock clusterTaskLock = new ReentrantReadWriteLock();
    private ClusterTask clusterTask;

    private OnSaveMapStateListener onSaveStateListener;
    private String mapMode = COUNTRY_MODE;
    private PublishRelay<Object> onItemsChange = PublishRelay.create();

    public CustomClusterManager(Context context, GoogleMap map) {
        super(context, map);

        this.map = map;
        cameraMoveObservable = createCameraMoveObservable();
        configureMap();

        algorithm = new CustomAlgorithm();
        clusterTask = new ClusterTask();
        renderer = new CustomClusterRenderer(context, map, this);
        setRenderer(renderer);
        clusterLoader = new ClusterLoader(map, this);

        map.setOnCameraIdleListener(this);
    }

    @Override
    public void setAlgorithm(Algorithm<LocationClusterItem> algorithm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClusterRenderer<LocationClusterItem> getRenderer() {
        return renderer;
    }

    @Override
    public Algorithm<LocationClusterItem> getAlgorithm() {
        return algorithm;
    }

    @Override
    public void clearItems() {
        algorithmLock.writeLock().lock();
        try {
            algorithm.clearItems();
        } finally {
            algorithmLock.writeLock().unlock();
        }
    }

    @Override
    public void addItems(Collection<LocationClusterItem> items) {
        algorithmLock.writeLock().lock();
        try {
            algorithm.addItems(items);
        } finally {
            algorithmLock.writeLock().unlock();
        }
        onItemsChange.accept(emit);
    }

    @Override
    public void addItem(LocationClusterItem myItem) {
        algorithmLock.writeLock().lock();
        try {
            algorithm.addItem(myItem);
        } finally {
            algorithmLock.writeLock().unlock();
        }
    }

    @Override
    public void removeItem(LocationClusterItem item) {
        algorithmLock.writeLock().lock();
        try {
            algorithm.removeItem(item);
        } finally {
            algorithmLock.writeLock().unlock();
        }
    }

    @Override
    public void cluster() {
        clusterTaskLock.writeLock().lock();
        try {
            clusterTask.cancel(true);
            clusterTask = new ClusterTask();
            clusterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, map.getCameraPosition().zoom);
        } finally {
            clusterTaskLock.writeLock().unlock();
        }
    }

    @Override
    public void onCameraIdle() {
        float zoom = map.getCameraPosition().zoom;
        LatLng target = map.getCameraPosition().target;
        onSaveStateListener.onSaveMapState(new MapState(target, zoom));
    }

    public void setMapMode(String mode) {
        mapMode = mode;
        clusterLoader.setIsCountry(mode.equals(COUNTRY_MODE));
    }

    public Observable<SupportSQLiteQuery> getQueryObservable() {
        return clusterLoader.getQueryObservable(cameraMoveObservable);
    }

    public Observable<CameraPosition> getCameraPositionObservable() {
        return cameraMoveObservable
                .map(o -> map.getCameraPosition())
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
                .map(o -> getAlgorithm().getItems())
                .flatMapSingle(locationClusterItems -> Observable.fromIterable(locationClusterItems)
                        .filter(item -> MapUtils.distanceMiles(map.getCameraPosition().target,
                                item.getPosition()) < 50)
                        .toList())
                .doOnNext(this::selectClusters);
    }

    private void selectClusters(List<LocationClusterItem> clusterItems) {
        Timber.d("selectClusters: ");
        renderer.selectItems(clusterItems);
        algorithm.setSelectedItems(clusterItems);
        cluster();
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
        //todo move out
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

    @SuppressLint("StaticFieldLeak")
    private class ClusterTask extends AsyncTask<Float, Void, Set<? extends Cluster<LocationClusterItem>>> {

        @Override
        protected Set<? extends Cluster<LocationClusterItem>> doInBackground(Float... zoom) {
            algorithmLock.readLock().lock();
            try {
                return algorithm.getClusters(zoom[0]);
            } finally {
                algorithmLock.readLock().unlock();
            }
        }

        @Override
        protected void onPostExecute(Set<? extends Cluster<LocationClusterItem>> clusters) {
            renderer.onClustersChanged(clusters);
        }
    }
}
