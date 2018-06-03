package io.github.vladimirmi.localradio.data.repositories;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.models.Country;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CountrySource;
import io.github.vladimirmi.localradio.data.source.LocationSource;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class LocationRepositoryImpl implements LocationRepository {

    private final CountrySource countrySource;
    private final Preferences preferences;
    private final LocationSource locationSource;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationRepositoryImpl(CountrySource countrySource,
                                  Preferences preferences,
                                  LocationSource locationSource) {
        this.countrySource = countrySource;
        this.preferences = preferences;
        this.locationSource = locationSource;
    }

    @Override
    public List<Country> getCountries() {
        return countrySource.getCountries();
    }

    @Override
    public void saveAutodetect(boolean enabled) {
        preferences.autodetect.put(enabled);
    }

    @Override
    public boolean isAutodetect() {
        return preferences.autodetect.get();
    }

    @Override
    public String getCountryCode() {
        return preferences.countryCode.get();
    }

    @Override
    public String getCity() {
        return preferences.city.get();
    }

    @Override
    public void saveCountryCodeCity(String countryCode, String city) {
        preferences.countryCode.put(countryCode);
        preferences.city.put(city);
    }

    @Override
    public boolean isServicesAvailable() {
        return locationSource.isServicesAvailable();
    }

    @Override
    public Completable checkCanGetLocation() {
        return locationSource.checkCanGetLocation();
    }

    @Override
    public Single<Pair<Float, Float>> getCoordinates() {
        return locationSource.getCoordinates();
    }

    @Override
    @Nullable
    public Pair<String, String> getCountryCodeCity(Pair<Float, Float> coordinates) {
        return locationSource.getCountryCodeCity(coordinates);
    }
}