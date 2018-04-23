package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;
import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils.ErrorCompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    private final SearchInteractor searchInteractor;
    private final StationsInteractor stationsInteractor;
    private final Country anyCountry = Country.any(Scopes.appContext());
    private final String anyCity = anyCountry.getCities().get(0);

    @Inject
    SearchPresenter(SearchInteractor searchInteractor,
                    StationsInteractor stationsInteractor) {
        this.searchInteractor = searchInteractor;
        this.stationsInteractor = stationsInteractor;
    }

    @Override
    protected void onFirstAttach(@Nullable SearchView view, CompositeDisposable disposables) {
        Objects.requireNonNull(view).setCountries(searchInteractor.getCountries());
        view.setAutodetect(searchInteractor.isAutodetect());
        view.setNewSearch(!searchInteractor.isDone());
        view.setSearchResult(stationsInteractor.getStations().size());

        view.setCountryName(searchInteractor.getCountryName());
        view.setCity(searchInteractor.getCity());
    }

    public void selectCountry(Country country) {
        List<String> cities = searchInteractor.findCities(Collections.singletonList(country));
        view.setCities(cities);
        view.setCity(cities.get(0));
        view.setCountryName(country.getName());
    }

    public void selectCountries(List<Country> countries) {
        view.setCities(searchInteractor.findCities(countries));
    }

    public void selectCity(String city) {
        if (!city.equals(anyCity)) {
            view.setCountryName(searchInteractor.findCountry(city).getName());
        }
        view.setCity(city);
    }

    @SuppressLint("CheckResult")
    public void setAutodetect(boolean autodetect) {
        view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .map(enabled -> enabled && autodetect)
                .doOnNext(enabled -> {
                    if (view != null) view.setAutodetect(enabled);
                })
                .firstOrError()
                .flatMapCompletable(searchInteractor::saveAutodetect)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ErrorCompletableObserver(view) {
                    @Override
                    public void onComplete() {
                        view.setCountryName(searchInteractor.getCountryName());
                        view.setCity(searchInteractor.getCity());
                    }
                });
    }

    public void search(String country, String city) {
//        if (country.isEmpty() || country.equals(anyCountry.getName())) {
//            country = searchInteractor.findCountry(city).getName();
//            view.setCountryName(country);
//        }
        searchInteractor.saveCountryNameCity(country, city);
        disposables.add(searchInteractor.searchStations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ErrorCompletableObserver(view) {
                    @Override
                    public void onComplete() {
                        view.setNewSearch(false);
                    }
                }));
    }

    public void refreshSearch() {
        disposables.add(searchInteractor.refreshStations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new ErrorCompletableObserver(view) {
                    @Override
                    public void onComplete() {
                    }
                }));
    }

    public void newSearch() {
        view.setNewSearch(true);
    }
}
