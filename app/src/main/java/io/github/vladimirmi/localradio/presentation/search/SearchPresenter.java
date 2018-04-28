package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;
import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.LocationInteractor;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    private final LocationInteractor locationInteractor;
    private final SearchInteractor searchInteractor;
    private final StationsInteractor stationsInteractor;

    private final Country anyCountry = Country.any(Scopes.appContext());
    private final String anyCity = anyCountry.getCities().get(0);

    @Inject
    SearchPresenter(LocationInteractor locationInteractor, SearchInteractor searchInteractor,
                    StationsInteractor stationsInteractor) {
        this.locationInteractor = locationInteractor;
        this.searchInteractor = searchInteractor;
        this.stationsInteractor = stationsInteractor;
    }

    @Override
    protected void onFirstAttach(@Nullable SearchView view, CompositeDisposable disposables) {
        view.setCountries(locationInteractor.getCountries());
        if (locationInteractor.isAutodetect()) {
            view.setAutoSearchDone(searchInteractor.isSearchDone());
        } else {
            view.setManualSearchDone(searchInteractor.isSearchDone());
        }
        view.setAutodetect(locationInteractor.isAutodetect());

        view.setCountryName(locationInteractor.getCountryName());
        view.setCity(locationInteractor.getCity());

        disposables.add(stationsInteractor.getStationsObs()
                .map(List::size).skip(1).firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<Integer>(view) {
                    @Override
                    public void onSuccess(Integer integer) {
                        view.setSearchResult(integer);
                    }
                }));
    }

    public void selectCountry(Country country) {
        List<String> cities = locationInteractor.findCities(Collections.singletonList(country));
        view.setCities(cities);
        view.setCity(cities.get(0));
        view.setCountryName(country.getName());
    }

    public void selectCountries(List<Country> countries) {
        view.setCities(locationInteractor.findCities(countries));
    }

    public void selectCity(String city) {
        if (!city.equals(anyCity)) {
            view.setCountryName(locationInteractor.findCountry(city).getName());
        }
        view.setCity(city);
    }

    @SuppressLint("CheckResult")
    public void setAutodetect(boolean autodetect) {
        view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .doOnNext(enabled -> {
                    // TODO: 4/27/18 action with settings to snackbar
                    if (!enabled) view.showMessage(R.string.need_permission);
                })
                .map(enabled -> enabled && autodetect)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(this::enableAutodetect)
                .filter(enabled -> enabled)
                .flatMapSingle(enabled -> searchInteractor.searchStations())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<List<Station>>(view) {
                    @Override
                    public void onNext(List<Station> stations) {
                        view.setCountryName(locationInteractor.getCountryName());
                        view.setSearchResult(stations.size());
                        view.setAutoSearchDone(true);
                    }
                });
    }

    public void search(String country, String city) {
        view.setSearching(true);
        locationInteractor.saveCountryNameCity(country, city);
        disposables.add(searchInteractor.searchStations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<List<Station>>(view) {
                    @Override
                    public void onSuccess(List<Station> stations) {
                        view.setSearchResult(stations.size());
                        view.setManualSearchDone(true);
                    }
                }));
    }

    public void refreshSearch() {
        view.setSearching(true);
        disposables.add(searchInteractor.refreshStations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<List<Station>>(view) {
                    @Override
                    public void onSuccess(List<Station> stations) {
                        view.setSearchResult(stations.size());
                    }
                }));
    }

    public void newSearch() {
        view.setManualSearchDone(false);
        view.resetSearchResult();
        searchInteractor.resetSearch();
    }

    private void enableAutodetect(boolean enabled) {
        locationInteractor.saveAutodetect(enabled);
        view.setAutodetect(enabled);
        view.setSearching(enabled);
        if (!enabled) {
            view.setAutoSearchDone(false);
            view.resetSearchResult();
            searchInteractor.resetSearch();
        }
    }
}
