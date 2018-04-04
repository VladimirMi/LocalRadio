package io.github.vladimirmi.localradio.data.repository;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.data.source.CountrySource;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class GeoLocationRepository {

    private CountrySource countrySource;

    @Inject
    public GeoLocationRepository(CountrySource countrySource) {
        this.countrySource = countrySource;
    }

    public List<Country> getCountries() {
        return countrySource.getCountries();
    }
}
