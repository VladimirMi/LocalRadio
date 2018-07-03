package io.github.vladimirmi.localradio.domain.repositories;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.List;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.data.models.Country;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 30.05.2018.
 */
public interface LocationRepository {

    List<Country> getCountries();

    void saveAutodetect(boolean enabled);

    boolean isAutodetect();

    String getCountryCode();

    String getCity();

    void saveCountryCodeCity(String countryCode, String city);

    boolean isServicesAvailable();

    Completable checkCanGetLocation();

    Single<Pair<Float, Float>> getCoordinates();

    @Nullable
    Pair<String, String> getCountryCodeCity(Pair<Float, Float> coordinates);

    List<LocationEntity> getLocations();
}
