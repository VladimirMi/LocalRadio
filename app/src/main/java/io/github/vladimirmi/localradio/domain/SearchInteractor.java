package io.github.vladimirmi.localradio.domain;

import java.util.ArrayList;
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

    public List<String> getCountriesName() {
        List<Country> countries = locationRepository.getCountries();
        List<String> names = new ArrayList<>(countries.size());
        for (Country country : countries) {
            names.add(country.getName());
        }
        return names;
    }
}
