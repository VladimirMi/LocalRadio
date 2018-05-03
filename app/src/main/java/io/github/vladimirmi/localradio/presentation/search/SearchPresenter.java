package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;
import android.annotation.SuppressLint;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Country;
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

    @Inject
    SearchPresenter(LocationInteractor locationInteractor, SearchInteractor searchInteractor,
                    StationsInteractor stationsInteractor) {
        this.locationInteractor = locationInteractor;
        this.searchInteractor = searchInteractor;
        this.stationsInteractor = stationsInteractor;
    }

    @Override
    protected void onFirstAttach(SearchView view, CompositeDisposable disposables) {
        view.setCountries(locationInteractor.getCountries());
        view.setAutodetect(locationInteractor.isAutodetect());

        disposables.add(searchInteractor.getSearchResults()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Integer>(view) {
                    @Override
                    public void onNext(Integer integer) {
                        if (hasView()) {
                            getView().setSearchResult(integer);
                            getView().setCountryName(locationInteractor.getCountryName());
                            getView().setCity(locationInteractor.getCity());
                            if (locationInteractor.isAutodetect()) {
                                getView().setAutoSearchDone(searchInteractor.isSearchDone());
                            } else {
                                getView().setManualSearchDone(searchInteractor.isSearchDone());
                            }
                        }
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
        Country country = locationInteractor.findCountry(city);
        if (country != null) {
            view.setCountryName(country.getName());
        }
        view.setCity(city);
    }

    @SuppressLint("CheckResult")
    public void setAutodetect(boolean autodetect) {
        searchInteractor.checkCanSearch()
                .andThen(view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION))
                .doOnNext(enabled -> {
                    // TODO: 4/27/18 action with settings to snackbar
                    if (!enabled) view.showMessage(R.string.need_permission);
                })
                .map(enabled -> enabled && autodetect)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(enabled -> {
                    enableAutodetect(enabled);
                    if (enabled) {
                        view.setSearching(true);
                        searchInteractor.searchStations();
                    }
                })
                .ignoreElements()
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.setSearching(false);
                    }
                });
    }

    public void search(String countryName, String city) {
        locationInteractor.saveCountryNameCity(countryName, city);

        disposables.add(locationInteractor.checkCanSearch()
                .andThen(searchInteractor.checkCanSearch())
                .doOnComplete(() -> {
                    view.setSearching(true);
                    searchInteractor.resetSearch();
                    searchInteractor.searchStations();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.setSearching(false);
                    }
                }));
    }

    public void refreshSearch() {
        disposables.add(searchInteractor.checkCanSearch()
                .doOnComplete(() -> {
                    view.setSearching(true);
                    searchInteractor.refreshStations();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.setSearching(false);
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
