package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.data.repository.GeoLocationRepository;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchInteractor {

    private GeoLocationRepository locationRepository;

    @Inject
    public SearchInteractor(GeoLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<Country> getCountries() {
        List<Country> countries = locationRepository.getCountries();
        countries.add(0, Country.any());
        return countries;
    }
}
