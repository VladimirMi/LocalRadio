package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;
import android.annotation.SuppressLint;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.LocationInteractor;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
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

    @Inject
    SearchPresenter(LocationInteractor locationInteractor,
                    SearchInteractor searchInteractor) {
        this.locationInteractor = locationInteractor;
        this.searchInteractor = searchInteractor;
    }

    @Override
    protected void onFirstAttach(SearchView view, CompositeDisposable disposables) {
        view.setCountries(locationInteractor.getCountriesName());
        String countryName = locationInteractor.getCountryName();
        view.setCities(locationInteractor.findCities(countryName));
        view.setCountryName(countryName);
        view.setCity(locationInteractor.getCity());
        view.setAutodetect(locationInteractor.isAutodetect());
        setSearchDone(searchInteractor.isSearchDone());

        disposables.add(searchInteractor.getSearchResults()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Integer>(view) {
                    @Override
                    public void onNext(Integer integer) {
                        if (hasView()) {
                            //noinspection ConstantConditions
                            getView().setSearchResult(integer);
                            getView().setCountryName(locationInteractor.getCountryName());
                            getView().setCity(locationInteractor.getCity());
                            setSearchDone(true);
                        }
                    }
                }));
    }

    public void selectCountry(String countryName) {
        List<String> cities = locationInteractor.findCities(countryName);
        view.setCities(cities);
        view.setCountryName(countryName);
        view.setCity(cities.get(0));
    }

    public void selectCity(String city) {
        String countryName = locationInteractor.findCountryName(city);
        if (countryName != null) {
            view.setCountryName(countryName);
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
                        setSearchDone(true);
                        searchInteractor.searchStations();
                    }
                })
                .ignoreElements()
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.setSearching(false);
                        setSearchDone(false);
                    }
                });
    }

    public void search(String countryName, String city) {
        locationInteractor.saveCountryNameCity(countryName, city);

        disposables.add(locationInteractor.checkCanSearch()
                .andThen(searchInteractor.checkCanSearch())
                .doOnComplete(() -> {
                    searchInteractor.resetSearch();
                    searchInteractor.searchStations();
                    view.setSearching(true);
                    setSearchDone(true);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.setSearching(false);
                        setSearchDone(false);
                    }
                }));
    }

    public void refreshSearch() {
        disposables.add(searchInteractor.checkCanSearch()
                .doOnComplete(() -> {
                    searchInteractor.refreshStations();
                    view.setSearching(true);
                    setSearchDone(true);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view) {
                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        view.setSearching(false);
                        setSearchDone(false);
                    }
                }));
    }

    public void newSearch() {
        view.setManualSearchDone(false);
        view.resetSearchResult();
        searchInteractor.resetSearch();
    }

    @SuppressWarnings("ConstantConditions")
    private void setSearchDone(boolean isSearchDone) {
        if (locationInteractor.isAutodetect()) {
            getView().setAutoSearchDone(isSearchDone);
        } else {
            getView().setManualSearchDone(isSearchDone);
        }
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
