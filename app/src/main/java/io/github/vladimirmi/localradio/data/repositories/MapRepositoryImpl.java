package io.github.vladimirmi.localradio.data.repositories;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Context;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.domain.repositories.MapRepository;
import io.github.vladimirmi.localradio.map.ClusterLoader;
import io.github.vladimirmi.localradio.map.CustomClusterRenderer;
import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.presentation.search.map.SearchMapPresenter;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public class MapRepositoryImpl implements MapRepository, GoogleMap.OnCameraIdleListener {

    private final Context context;
    private final Preferences preferences;
    private final MapWrapper mapWrapper;

    private ClusterLoader clusterLoader;
    private CustomClusterRenderer renderer;

    //todo move to different scope
    @Inject
    public MapRepositoryImpl(Context context, Preferences preferences) {
        this.context = context;
        this.preferences = preferences;
        mapWrapper = new MapWrapper();
    }

    @Override
    public void saveMapMode(String mapMode) {
        preferences.mapMode.put(mapMode);
        clusterLoader.setIsCountry(mapMode.equals(SearchMapPresenter.COUNTRY_MODE));
    }

    @Override
    public String getMapMode() {
        return preferences.mapMode.get();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mapWrapper.setMap(map);
        restoreMap();

        ClusterManager<LocationClusterItem> clusterManager = new ClusterManager<>(context, map);
        renderer = new CustomClusterRenderer(context, map, clusterManager);
        clusterManager.setRenderer(renderer);
        map.setOnCameraIdleListener(this);

        clusterLoader = new ClusterLoader(map, clusterManager);
        clusterLoader.setIsCountry(getMapMode().equals(SearchMapPresenter.COUNTRY_MODE));
    }

    @Override
    public void addClusters(List<LocationClusterItem> clusterItems) {
        clusterLoader.addClusters(clusterItems);
    }

    @Override
    public Observable<SupportSQLiteQuery> getQueryObservable() {
        return clusterLoader.getQueryObservable(mapWrapper.getCameraMoveObservable());
    }

    @Override
    public Observable<Float> getZoomObservable() {
        return mapWrapper.getCameraMoveObservable()
                .map(o -> mapWrapper.getMap().getCameraPosition().zoom);
    }

    @Override
    public List<LocationClusterItem> getSelectedItems() {
        return Collections.emptyList();
    }

    @Override
    public void onCameraIdle() {
        clusterLoader.onCameraIdle();
        float zoom = mapWrapper.getMap().getCameraPosition().zoom;
        LatLng target = mapWrapper.getMap().getCameraPosition().target;
        preferences.mapLat.put((float) target.latitude);
        preferences.mapLong.put((float) target.longitude);
        preferences.mapZoom.put(zoom);
    }

    private void restoreMap() {
        LatLng position = new LatLng(preferences.mapLat.get(), preferences.mapLong.get());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(position, preferences.mapZoom.get());
        mapWrapper.getMap().moveCamera(cameraUpdate);
    }
}
