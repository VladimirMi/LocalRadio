package io.github.vladimirmi.localradio.domain.interactors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.models.Country;
import io.github.vladimirmi.localradio.data.repositories.LocationRepositoryImpl;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 24.04.2018.
 */
public class LocationInteractor {

    private static final String UNLISTED_CITY = "{unlisted}";

    private final LocationRepositoryImpl locationRepository;

    // TODO: 5/2/18 Create resource manager
    private String unlistedCity = Scopes.appContext().getString(R.string.unlisted_city);

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationInteractor(LocationRepositoryImpl locationRepository) {
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
            return countryCode;
        }
        return new Locale("", countryCode).getDisplayCountry();
    }

    public String getCity() {
        String city = locationRepository.getCity();
        if (city.equals(UNLISTED_CITY)) return unlistedCity;
        return city;
    }

    public void saveCountryNameCity(String countryName, String cityName) {
        Timber.e("saveCountryNameCity: %s, %s", countryName, cityName);
        String countryCode = "";
        if (!countryName.isEmpty()) {
            for (Country country : locationRepository.getCountries()) {
                if (country.getName().equals(countryName)) {
                    countryCode = country.getIsoCode();
                    break;
                }
            }
        }
        if (cityName.equals(unlistedCity)) cityName = UNLISTED_CITY;
        locationRepository.saveCountryCodeCity(countryCode, cityName);
    }

    public String findCountryName(String city) {
        if (city.isEmpty() || city.equals(unlistedCity)) return "";

        for (Country country : locationRepository.getCountries()) {
            if (country.getCities().contains(city)) {
                return country.getName();
            }
        }
        throw new IllegalStateException();
    }

    public List<String> findCities(String countryName) {
        Set<String> cities = new TreeSet<>();
        for (Country country : findCountries(countryName)) {
            cities.addAll(country.getCities());
        }
        boolean hasUnlistedCity = cities.remove(UNLISTED_CITY);

        ArrayList<String> cityList = new ArrayList<>(cities.size() + (hasUnlistedCity ? 1 : 0));
        if (hasUnlistedCity) cityList.add(unlistedCity);
        cityList.addAll(cities);
        return cityList;
    }

    public Completable checkCanSearch() {
        if (getCountryName().isEmpty() && getCity().isEmpty()) {
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
        if (countryName.isEmpty()) {
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
