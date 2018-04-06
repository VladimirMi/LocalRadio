package io.github.vladimirmi.localradio.presentation.search;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    private SearchInteractor interactor;
    private List<Country> countries;
    private Country anyCountry = Country.any(Scopes.appContext());
    private String anyCity = anyCountry.getCities().get(0);

    @Inject
    public SearchPresenter(SearchInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    protected void onAttach(SearchView view) {
        countries = interactor.getCountries();
        view.setCountries(countries);
    }

    public void selectCountry(Country country) {
        List<String> cities;
        if (country.equals(anyCountry)) {
            cities = citiesFromCountries(countries);
        } else {
            cities = citiesFromCountries(Collections.singletonList(country));
        }
        view.setCities(cities);
        view.setCity(cities);
    }

    public void selectCountries(List<Country> countries) {
        view.setCities(citiesFromCountries(countries));
    }

    public void selectCity(String city) {
        if (!city.equals(anyCity)) {
            view.setCountry(findCountryForCity(city, countries).getName());
        }
    }

    private List<String> citiesFromCountries(List<Country> countries) {
        List<String> cities = new ArrayList<>();

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

    @NonNull
    private Country findCountryForCity(String city, List<Country> allCountries) {
        for (Country country : allCountries) {
            if (country.getCities().contains(city)) {
                return country;
            }
        }
        throw new IllegalStateException();
    }
}
