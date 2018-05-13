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
import timber.log.Timber;

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
        view.setCountrySuggestions(locationInteractor.getCountriesName());
        String countryName = locationInteractor.getCountryName();
        view.setCitySuggestions(locationInteractor.findCities(countryName));
        view.setCountryName(countryName);
        view.setCity(locationInteractor.getCity());
        view.setAutodetect(locationInteractor.isAutodetect());
        setSearchDone(searchInteractor.isSearchDone());
        view.enableAutodetect(locationInteractor.isServicesAvailable());
    }

    @Override
    protected void onAttach(SearchView view) {
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
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view));
    }


    @SuppressLint("CheckResult")
    public void refreshSearch() {
        searchInteractor.checkCanSearch()
                .doOnComplete(() -> {
                    setSearchDone(true);
                    view.resetSearchResult();
                })
                .andThen(searchInteractor.refreshStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view));
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
        locationInteractor.saveCountryNameCity(countryName, city);

        locationInteractor.checkCanSearch()
                .andThen(searchInteractor.checkCanSearch())
                .doOnComplete(() -> setSearchDone(true))
                .andThen(searchInteractor.searchStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view));
    }

    private void newSearch() {
        Timber.e("newSearch: ");
        searchInteractor.resetSearch();
        view.resetSearchResult();
        setSearchDone(false);
    }

    private void handleSearchResults(Integer integer) {
        view.setSearchResult(integer);
        view.setCountryName(locationInteractor.getCountryName());
        String city = locationInteractor.getCity();
        view.setCity(city);
        view.showCity(!city.equals(locationInteractor.anyCity)
                || !locationInteractor.isAutodetect());
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
        Timber.e("setSearchDone: " + isSearchDone);
        view.setSearchDone(isSearchDone);
        if (!isSearchDone) view.showCity(true);
        view.enableRefresh(isSearchDone);
    }
}
