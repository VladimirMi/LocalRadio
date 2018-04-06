package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;
import android.annotation.SuppressLint;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
        view.setAutodetect(interactor.getAutodetect());

        view.setCountry(interactor.getCountry());
        view.setCity(Collections.singletonList(interactor.getCity()));
    }

    public void selectCountry(Country country) {
        List<String> cities = interactor.findCities(Collections.singletonList(country));
        view.setCities(cities);
        view.setCity(cities);
    }

    public void selectCountries(List<Country> countries) {
        view.setCities(interactor.findCities(countries));
    }

    public void selectCity(String city) {
        if (!city.equals(anyCity)) {
            view.setCountry(interactor.findCountry(city).getName());
        }
    }

    @SuppressLint("CheckResult")
    public void setAutodetect(boolean autodetect) {
        if (autodetect) {
            view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                    .doOnNext(enabled -> {
                        if (view != null) view.setAutodetect(enabled);
                    })
                    .flatMapCompletable(interactor::saveAutodetect)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> view.setCountry(interactor.getCountry()));
        } else {
            interactor.saveAutodetect(false);
            view.setAutodetect(false);
        }
    }
}
