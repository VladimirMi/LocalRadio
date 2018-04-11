package io.github.vladimirmi.localradio.domain;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.data.repository.GeoLocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchInteractor {

    private final GeoLocationRepository locationRepository;
    private final StationsRepository stationsRepository;

    private Country anyCountry = Country.any(Scopes.appContext());
    private String anyCity = anyCountry.getCities().get(0);

    @Inject
    public SearchInteractor(GeoLocationRepository locationRepository,
                            StationsRepository stationsRepository) {
        this.locationRepository = locationRepository;
        this.stationsRepository = stationsRepository;
    }

    public List<Country> getCountries() {
        return locationRepository.getCountries();
    }

    public Completable saveAutodetect(boolean enabled) {
        locationRepository.saveAutodetect(enabled);
        if (!enabled) locationRepository.saveCountryCodeCity("", "");
        return stationsRepository.refreshStations();
    }

    public boolean isAutodetect() {
        return locationRepository.isAutodetect();
    }

    public boolean isDone() {
        return isAutodetect() || !locationRepository.getCountryCode().isEmpty();
    }

    public String getCountryName() {
        String countryCode = locationRepository.getCountryCode();
        if (countryCode.isEmpty()) {
            return anyCountry.getName();
        }
        return new Locale("", countryCode).getDisplayCountry();
    }

    public String getCity() {
        String city = locationRepository.getCity();
        if (city.isEmpty()) return anyCity;
        return city;
    }

    public Completable search(String countryName, String cityName) {
        saveCountryNameCity(countryName, cityName);
        return performSearch();
    }

    public Completable performSearch() {
        if (locationRepository.getCountryCode().isEmpty() && locationRepository.getCity().isEmpty()) {
            return Completable.error(new MessageException(R.string.error_specify_location));
        }
        return stationsRepository.refreshStations();
    }

    private void saveCountryNameCity(String countryName, String cityName) {
        String countryCode = "";
        for (Country country : getCountries()) {
            if (country.getName().equals(countryName) && !country.equals(anyCountry)) {
                countryCode = country.getIsoCode();
                break;
            }
        }
        if (cityName.equals(anyCity)) cityName = "";
        locationRepository.saveCountryCodeCity(countryCode, cityName);
    }

    @NonNull
    public Country findCountry(String city) {
        for (Country country : locationRepository.getCountries()) {
            if (country.getCities().contains(city)) {
                return country;
            }
        }
        throw new IllegalStateException();
    }

    public List<String> findCities(List<Country> countries) {
        Set<String> cities = new TreeSet<>();

        if (countries.size() == 1 && countries.get(0).equals(anyCountry)) {
            countries = locationRepository.getCountries();
        }

        for (Country country : countries) {
            cities.addAll(country.getCities());
        }
        cities.remove(anyCity);
        ArrayList<String> cityList = new ArrayList<>(cities);
        if (cities.size() > 1 || cities.size() == 0) {
            cityList.add(0, anyCity);
        }
        return cityList;
    }
}
