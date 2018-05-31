package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;
import android.annotation.SuppressLint;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    private final LocationInteractor locationInteractor;
    private final SearchInteractor searchInteractor;
    private final StationsInteractor stationsInteractor;

    @Inject
    SearchPresenter(LocationInteractor locationInteractor,
                    SearchInteractor searchInteractor,
                    StationsInteractor stationsInteractor) {
        this.locationInteractor = locationInteractor;
        this.searchInteractor = searchInteractor;
        this.stationsInteractor = stationsInteractor;
    }

    @Override
    protected void onAttach(SearchView view) {
        view.setCountrySuggestions(locationInteractor.getCountriesName());

        disposables.add(searchInteractor.getSearchResultObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<SearchResult>(view) {
                    @Override
                    public void onNext(SearchResult result) {
                        handleSearchResult(result);
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
                .doOnNext(this::setAutodetect)
                .filter(enabled -> enabled)
                .flatMapCompletable(enabled -> searchInteractor.searchStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView()));
    }


    @SuppressLint("CheckResult")
    public void refreshSearch() {
        searchInteractor.checkCanSearch()
                .andThen(searchInteractor.refreshStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView()));
    }

    public void search(String countryName, String city) {
        if (!searchInteractor.getSearchState().isSearchDone()) {
            performSearch(countryName, city);
        } else {
            searchInteractor.resetSearch();
            newSearchState();
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
                .andThen(searchInteractor.searchStations())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView()));
    }

    private void handleSearchResult(SearchResult searchResult) {
        switch (searchResult.state) {
            case NOT_DONE:
                newSearchState();
                break;
            case LOADING:
                loadingState();
                break;
            case AUTO_DONE:
            case MANUAL_DONE:
                view.setSearchResult(searchResult.message);
                selectCountry(locationInteractor.getCountryName());
                view.setCity(locationInteractor.getCity());

                if (searchResult.state == SearchResult.State.AUTO_DONE) {
                    searchDoneAutoState();
                } else {
                    searchDoneManualState();
                }
                break;
        }
    }

    private void setAutodetect(boolean enabled) {
        locationInteractor.saveAutodetect(enabled);
        view.setAutodetect(enabled);
        if (!enabled) {
            searchInteractor.resetSearch();
            newSearchState();
        }
    }

    private void newSearchState() {
        view.resetSearchResult();
        view.setSearchDone(false);
        view.showSearchBtn(true);
    }

    private void loadingState() {
        view.resetSearchResult();
        view.setSearching(true);
        view.enableAutodetect(false);
        view.setSearchDone(true);
        view.showSearchBtn(false);
    }

    private void searchDoneAutoState() {
        view.setSearching(false);
        view.enableAutodetect(locationInteractor.isServicesAvailable());
        view.setSearchDone(true);
        view.showSearchBtn(false);
        view.setAutodetect(true);
    }

    private void searchDoneManualState() {
        view.setSearching(false);
        view.enableAutodetect(locationInteractor.isServicesAvailable());
        view.setSearchDone(true);
        view.showSearchBtn(true);
    }
}
