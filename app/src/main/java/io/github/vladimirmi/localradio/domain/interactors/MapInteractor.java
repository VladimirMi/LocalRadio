package io.github.vladimirmi.localradio.domain.interactors;

import android.arch.persistence.db.SupportSQLiteQuery;

import com.google.android.gms.maps.GoogleMap;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.domain.repositories.MapRepository;
import io.github.vladimirmi.localradio.presentation.search.map.SearchMapPresenter;
import io.reactivex.Observable;


/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public class MapInteractor {

    private final MapRepository mapRepository;

    @Inject
    public MapInteractor(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public void onMapReady(GoogleMap map) {
        mapRepository.onMapReady(map);
    }

    public void addClusters(List<LocationClusterItem> clusterItems) {
        mapRepository.addClusters(clusterItems);
    }

    public Observable<SupportSQLiteQuery> getQueryObservable() {
        return mapRepository.getQueryObservable();
    }

    public Observable<Float> getZoomObservable() {
        return mapRepository.getZoomObservable()
                .filter(zoom -> getMapMode().equals(SearchMapPresenter.RADIUS_MODE))
                .distinctUntilChanged();
    }

    public String getMapMode() {
        return mapRepository.getMapMode();
    }

    public void saveMapMode(String mapMode) {
        mapRepository.saveMapMode(mapMode);
    }
}
