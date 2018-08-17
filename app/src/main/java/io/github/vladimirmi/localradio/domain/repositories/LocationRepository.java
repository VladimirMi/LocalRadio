package io.github.vladimirmi.localradio.domain.repositories;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.util.Pair;

import java.util.List;
import java.util.Set;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.map.MapState;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 30.05.2018.
 */
public interface LocationRepository {

    void saveMapMode(String mode);

    String getMapMode();

    void saveMapState(MapState mapState);

    MapState getMapState();

    Single<List<LocationEntity>> loadClusters(SupportSQLiteQuery query);

    Single<List<LocationEntity>> getCountries();

    Single<LocationEntity> getCountry(String isoCode);

    Single<List<LocationEntity>> getCities(String isoCode);

    void saveLocations(Set<String> locationsId);

    Single<List<LocationEntity>> getSavedLocations();

    void saveAutodetect(boolean enabled);

    boolean isAutodetect();

    boolean isServicesAvailable();

    Completable checkCanGetLocation();

    Single<Pair<Float, Float>> getCurrentLocation();
}
