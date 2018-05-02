package io.github.vladimirmi.localradio.data.repository;

import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.data.entity.Station;
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

    public void saveCountryCodeCity(String countryCode, String city) {
        preferences.countryCode.put(countryCode);
        preferences.city.put(city);
    }

    public void saveCountryCodeCity(List<Station> stations) {
        if (!stations.isEmpty()) {
            saveCountryCodeCity(stations.get(0).getCountryCode(), "");
        }
    }

    public void saveCountryCodeCity(Pair<String, String> countryCity) {
        String countryCode = countryCity.first;
        String city = hasCountryCity(countryCity) ? countryCity.second : "";
        saveCountryCodeCity(countryCode, city);
    }

    public Single<Pair<Float, Float>> getCoordinates() {
        return locationSource.getCoordinates();
    }

    @Nullable
    public Pair<String, String> getCountryCodeCity(Pair<Float, Float> coordinates) {
        return locationSource.getCountryCodeCity(coordinates);
    }

    private boolean hasCountryCity(Pair<String, String> countryCity) {
        for (Country country : getCountries()) {
            if (country.getIsoCode().equals(countryCity.first)) {
                for (String city : country.getCities()) {
                    if (city.equals(countryCity.second)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
