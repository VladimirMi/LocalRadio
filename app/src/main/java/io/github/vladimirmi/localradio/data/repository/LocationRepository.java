package io.github.vladimirmi.localradio.data.repository;

import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CountrySource;
import io.github.vladimirmi.localradio.data.source.LocationSource;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class LocationRepository {

    private final CountrySource countrySource;
    private final Preferences preferences;
    private final LocationSource locationSource;

    @Inject
    public LocationRepository(CountrySource countrySource,
                              Preferences preferences,
                              LocationSource locationSource) {
        this.countrySource = countrySource;
        this.preferences = preferences;
        this.locationSource = locationSource;
    }

    public List<Country> getCountries() {
        return countrySource.getCountries();
    }

    public void saveAutodetect(boolean enabled) {
        preferences.autodetect.put(enabled);
    }

    public boolean isAutodetect() {
        return preferences.autodetect.get();
    }

    public String getCountryCode() {
        return preferences.countryCode.get();
    }

    public String getCity() {
        return preferences.city.get();
    }

    public void saveCountryCodeCity(String country, String city) {
        preferences.countryCode.put(country);
        preferences.city.put(city);
    }

    public Single<Pair<Float, Float>> getCoordinates() {
        return locationSource.getLastLocation()
                .map(location -> {
                    float latitude = Math.round(location.getLatitude() * 100.0) / 100.0f;
                    float longitude = Math.round(location.getLongitude() * 100.0) / 100.0f;
                    return new Pair<>(latitude, longitude);
                });
    }
}
