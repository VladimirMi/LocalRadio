package io.github.vladimirmi.localradio.domain;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
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

/**
 * Created by Vladimir Mikhalev 24.04.2018.
 */
public class LocationInteractor {

    private static final String UNLISTED_CITY = "{unlisted}";
    private Country anyCountry = Country.any();
    public String anyCity = anyCountry.getCities().get(0);

    private final LocationRepository locationRepository;

    // TODO: 5/2/18 Create resource manager
    private String unlistedCity = Scopes.appContext().getString(R.string.unlisted_city);

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationInteractor(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public List<String> getCountriesName() {
        List<Country> countries = locationRepository.getCountries();
        List<String> countriesName = new ArrayList<>(countries.size());

        for (Country country : countries) {
            countriesName.add(country.getName());
        }
        return countriesName;
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
        if (city.equals(UNLISTED_CITY)) return unlistedCity;
        return city;
    }

    public void saveCountryNameCity(String countryName, String cityName) {
        String countryCode = "";
        if (!countryName.equals(anyCountry.getName())) {
            for (Country country : locationRepository.getCountries()) {
                if (country.getName().equals(countryName)) {
                    countryCode = country.getIsoCode();
                    break;
                }
            }
        }
        if (cityName.equals(anyCity)) cityName = "";
        if (cityName.equals(unlistedCity)) cityName = UNLISTED_CITY;
        locationRepository.saveCountryCodeCity(countryCode, cityName);
    }

    @Nullable
    public String findCountryName(String city) {
        if (city.equals(anyCity) || city.equals(unlistedCity)) return null;

        for (Country country : locationRepository.getCountries()) {
            if (country.getCities().contains(city)) {
                return country.getName();
            }
        }
        throw new IllegalStateException();
    }

    public List<String> findCities(String countryName) {
        Set<String> cities = new TreeSet<>();
        List<Country> countries = findCountries(countryName);

        for (Country country : countries) {
            cities.addAll(country.getCities());
        }
        cities.remove(anyCity);
        // TODO: 5/2/18 change "" to "{unlisted}" in countries.json
        boolean hasUnlistedCity = cities.remove("");

        ArrayList<String> cityList = new ArrayList<>(cities.size() + 2);
        if (cities.size() > 1) cityList.add(anyCity);
        if (hasUnlistedCity) cityList.add(unlistedCity);
        cityList.addAll(cities);
        return cityList;
    }

    public Completable checkCanSearch() {
        if (getCountryName().equals(anyCountry.getName()) && getCity().equals(anyCity)) {
            return Completable.error(new MessageException(R.string.error_specify_location));
        } else {
            return Completable.complete();
        }
    }

    public boolean isServicesAvailable() {
        return locationRepository.isServicesAvailable();
    }

    public Completable checkCanGetLocation() {
        return locationRepository.checkCanGetLocation();
    }

    private List<Country> findCountries(String countryName) {
        if (countryName.equals(anyCountry.getName())) {
            return locationRepository.getCountries();
        } else {
            for (Country country : locationRepository.getCountries()) {
                if (country.getName().equals(countryName)) {
                    return Collections.singletonList(country);
                }
            }
        }
        return Collections.emptyList();
    }
}
