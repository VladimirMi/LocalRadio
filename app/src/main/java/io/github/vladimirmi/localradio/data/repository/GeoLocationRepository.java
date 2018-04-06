package io.github.vladimirmi.localradio.data.repository;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CountrySource;
import io.github.vladimirmi.localradio.data.source.LocationSource;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class GeoLocationRepository {

    private CountrySource countrySource;
    private LocationSource locationSource;
    private Preferences preferences;

    @Inject
    public GeoLocationRepository(CountrySource countrySource,
                                 LocationSource locationSource,
                                 Preferences preferences) {
        this.countrySource = countrySource;
        this.locationSource = locationSource;
        this.preferences = preferences;
    }

    public List<Country> getCountries() {
        return countrySource.getCountries();
    }

    public void saveAutodetect(boolean enabled) {
        preferences.autodetectPref.put(enabled);
    }

    public boolean getAutodetect() {
        return preferences.autodetectPref.get();
    }

    public String getLocationCountry() {
        return "Empty";
    }
}
