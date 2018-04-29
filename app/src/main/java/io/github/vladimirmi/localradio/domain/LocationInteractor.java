package io.github.vladimirmi.localradio.domain;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 24.04.2018.
 */
public class LocationInteractor {

    private final LocationRepository locationRepository;
    private final SearchInteractor searchInteractor;

    private Country anyCountry = Country.any(Scopes.appContext());
    private String anyCity = anyCountry.getCities().get(0);

    @Inject
    public LocationInteractor(LocationRepository locationRepository,
                              SearchInteractor searchInteractor) {
        this.locationRepository = locationRepository;
        this.searchInteractor = searchInteractor;
    }

    public List<Country> getCountries() {
        return locationRepository.getCountries();
    }

    public void saveAutodetect(boolean enabled) {
        locationRepository.saveAutodetect(enabled);
    }

    public boolean isAutodetect() {
        return locationRepository.isAutodetect();
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

    public void saveCountryNameCity(String countryName, String cityName) {
        Timber.e("saveCountryNameCity: %s, %s", countryName, cityName);
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

    @Nullable
    public Country findCountry(String city) {
        if (city.equals(anyCity)) return null;

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

    public Completable checkCanSearch() {
        if (getCountryName().equals(anyCountry.getName()) && getCity().equals(anyCity)) {
            return Completable.error(new MessageException(R.string.error_specify_location));
        } else {
            return Completable.complete();
        }
    }
}
