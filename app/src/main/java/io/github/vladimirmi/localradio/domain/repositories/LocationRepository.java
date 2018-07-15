package io.github.vladimirmi.localradio.domain.repositories;

import android.util.Pair;

import java.util.List;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 30.05.2018.
 */
public interface LocationRepository {

    void saveMapMode(String mode);

    String getMapMode();

    Observable<List<LocationEntity>> getCountries();

    Observable<List<LocationEntity>> getCountry(String isoCode);

    Observable<List<LocationEntity>> getCities(String country);

    void saveLocations(int... locationId);

    Observable<List<LocationEntity>> getSavedLocations();

    void saveAutodetect(boolean enabled);

    boolean isAutodetect();

    boolean isServicesAvailable();

    Completable checkCanGetLocation();

    Single<Pair<Float, Float>> getCurrentLocation();
}
