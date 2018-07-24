package io.github.vladimirmi.localradio.domain.repositories;

import android.arch.persistence.db.SupportSQLiteQuery;

import com.google.android.gms.maps.GoogleMap;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public interface MapRepository {

    void saveMapMode(String mapMode);

    String getMapMode();

    void onMapReady(GoogleMap map);

    void addClusters(List<LocationClusterItem> clusterItems);

    Observable<SupportSQLiteQuery> getQueryObservable();

    Observable<Float> getZoomObservable();

    List<LocationClusterItem> getSelectedItems();
}
