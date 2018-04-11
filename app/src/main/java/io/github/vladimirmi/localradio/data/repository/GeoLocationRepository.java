package io.github.vladimirmi.localradio.data.repository;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CountrySource;
import io.github.vladimirmi.localradio.data.source.LocationSource;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class GeoLocationRepository {

    private final RestService restService;
    private final CountrySource countrySource;
    private final LocationSource locationSource;
    private final Preferences preferences;

    @Inject
    public GeoLocationRepository(RestService restService,
                                 CountrySource countrySource,
                                 LocationSource locationSource,
                                 Preferences preferences) {
        this.restService = restService;
        this.countrySource = countrySource;
        this.locationSource = locationSource;
        this.preferences = preferences;
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
}
