package io.github.vladimirmi.localradio.domain;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.data.repository.GeoLocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.di.Scopes;
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
        return stationsRepository.refreshStations();
    }

    public boolean isAutodetect() {
        return locationRepository.isAutodetect();
    }

    public boolean isDone() {
        return isAutodetect() || !getCountry().isEmpty();
    }

    public String getCountry() {
        String countryCode = locationRepository.getCountryCode();
        if (countryCode.isEmpty()) {
            return countryCode;
        }
        return new Locale("", countryCode).getDisplayCountry();
    }

    public String getCity() {
        return locationRepository.getCity();
    }

    public Completable search(String countryName, String cityName) {
        saveCountryCodeCity(countryName, cityName);
        return stationsRepository.refreshStations();
    }

    private void saveCountryCodeCity(String countryName, String cityName) {
        String countryCode = "";
        for (Country country : getCountries()) {
            if (country.getName().equals(countryName)) {
                countryCode = country.getIsoCode();
            }
        }
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
        List<String> cities = new ArrayList<>();

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
