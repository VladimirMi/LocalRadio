package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;
import android.annotation.SuppressLint;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

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
    protected void onAttach(SearchView view, boolean isFirstAttach) {
        view.setCountrySuggestions(locationInteractor.getCountriesName());
        String countryName = locationInteractor.getCountryName();
        view.setCitySuggestions(locationInteractor.findCities(countryName));
        view.setCountryName(countryName);
        view.setCity(locationInteractor.getCity());
        view.setAutodetect(locationInteractor.isAutodetect());
        view.showSearchBtn(!locationInteractor.isAutodetect());
        setSearchDone(searchInteractor.isSearchDone());
        view.enableAutodetect(locationInteractor.isServicesAvailable());


        disposables.add(searchInteractor.getSearchResults()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Integer>(view) {
                    @Override
                    public void onNext(Integer integer) {
                        handleSearchResults(integer);
                    }
                }));

        disposables.add(searchInteractor.isSearching()
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isSearching) {
                        handleIsSearching(isSearching);
                    }
                }));
    }

    public void selectCountry(String countryName) {
        List<String> cities = locationInteractor.findCities(countryName);
        view.setCitySuggestions(cities);
        view.setCountryName(countryName);
    }

    public void selectCity(String city) {
        String countryName = locationInteractor.findCountryName(city);
        if (!countryName.isEmpty()) {
            view.setCountryName(countryName);
        }
        // TODO: 5/17/18 set country suggestions
        view.setCity(city);
    }

    @SuppressLint("CheckResult")
    public void enableAutodetect(boolean autodetect) {
        searchInteractor.checkCanSearch()
                .andThen(locationInteractor.checkCanGetLocation())
                .andThen(view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION))
                .delay(300, TimeUnit.MILLISECONDS)
                .doOnNext(enabled -> {
                    // TODO: 4/27/18 add action that opens settings to the snackbar
                    if (!enabled) view.showMessage(R.string.need_permission);
                })
                .map(enabled -> enabled && autodetect)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(enabled -> {
                    setAutodetect(enabled);
                    if (enabled) setSearchDone(true);
                })
                .filter(enabled -> enabled)
                .flatMapCompletable(enabled -> searchInteractor.searchStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView()));
    }


    @SuppressLint("CheckResult")
    public void refreshSearch() {
        searchInteractor.checkCanSearch()
                .doOnComplete(() -> {
                    setSearchDone(true);
                    view.resetSearchResult();
                })
                .andThen(searchInteractor.refreshStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView()));
    }

    public void search(String countryName, String city) {
        if (!searchInteractor.isSearchDone()) {
            performSearch(countryName, city);
        } else {
            newSearch();
        }
    }

    @SuppressLint("CheckResult")
    private void performSearch(String countryName, String city) {
        if (countryName.isEmpty()) {
            countryName = locationInteractor.findCountryName(city);
            view.setCountryName(countryName);
        }
        locationInteractor.saveCountryNameCity(countryName, city);

        locationInteractor.checkCanSearch()
                .andThen(searchInteractor.checkCanSearch())
                .doOnComplete(() -> setSearchDone(true))
                .andThen(searchInteractor.searchStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView()));
    }

    private void newSearch() {
        searchInteractor.resetSearch();
        view.resetSearchResult();
        setSearchDone(false);
    }

    private void handleSearchResults(Integer integer) {
        view.setSearchResult(integer);
        selectCountry(locationInteractor.getCountryName());
        selectCity(locationInteractor.getCity());
    }

    private void handleIsSearching(boolean isSearching) {
        view.setSearching(isSearching);
        if (!isSearching) {
            view.enableSearch(true);
            view.enableAutodetect(locationInteractor.isServicesAvailable());
            if (!searchInteractor.isSearchDone()) {
                handleFailedSearch();
            }
        } else {
            view.enableSearch(false);
            view.enableAutodetect(false);
        }
    }

    private void handleFailedSearch() {
        if (locationInteractor.isAutodetect()) {
            setAutodetect(false);
        } else {
            newSearch();
        }
    }

    private void setAutodetect(boolean enabled) {
        locationInteractor.saveAutodetect(enabled);
        view.setAutodetect(enabled);
        view.showSearchBtn(!enabled);
        if (!enabled) newSearch();
    }

    private void setSearchDone(boolean isSearchDone) {
        view.setSearchDone(isSearchDone);
        view.enableRefresh(isSearchDone);
    }
}
