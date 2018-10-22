package io.github.vladimirmi.localradio.domain.repositories;

import android.util.Pair;

import java.util.List;
import java.util.Set;

import androidx.sqlite.db.SupportSQLiteQuery;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.map.MapPosition;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 30.05.2018.
 */
public interface LocationRepository {

    void saveMapMode(String mode);

    String getMapMode();

    void saveMapPosition(MapPosition mapState);

    MapPosition getMapPosition();

    Single<List<LocationEntity>> loadClusters(SupportSQLiteQuery query);

    Single<List<LocationEntity>> getCountries();

    Single<LocationEntity> getCountry(String isoCode);

    Single<List<LocationEntity>> getCities(String isoCode);

    void saveLocations(Set<String> locationsId);

    Single<List<LocationEntity>> getSavedLocations();

    boolean isServicesAvailable();

    Completable checkCanGetLocation();

    Single<Pair<Float, Float>> getCurrentLocation();

    Pair<String, String> getCountryCodeCity(MapPosition coordinates);
}
