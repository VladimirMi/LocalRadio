package io.github.vladimirmi.localradio.presentation.search;

import android.Manifest;

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
        view.setCountrySuggestions(locationInteractor.getCountriesName());
        String countryName = locationInteractor.getCountryName();
        view.setCitySuggestions(locationInteractor.findCities(countryName));
        view.setCountryName(countryName);
        view.setCity(locationInteractor.getCity());
        view.setAutodetect(locationInteractor.isAutodetect());
        setSearchDone(searchInteractor.isSearchDone());
    }

    @Override
    protected void onAttach(SearchView view) {
        disposables.add(searchInteractor.getSearchResults()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Integer>(view) {
                    @Override
                    public void onNext(Integer integer) {
                        view.setSearchResult(integer);
                        view.setCountryName(locationInteractor.getCountryName());
                        String city = locationInteractor.getCity();
                        view.setCity(city);
                        view.showCity(!city.equals(locationInteractor.anyCity)
                                || !locationInteractor.isAutodetect());
                    }
                }));

        disposables.add(searchInteractor.isSearching()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isSearching) {
                        view.setSearching(isSearching);
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

    public void setAutodetect(boolean autodetect) {
        disposables.add(searchInteractor.checkCanSearch()
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
                        setSearchDone(true);
                        searchInteractor.searchStations();
                    }
                })
                .ignoreElements()
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    public void search(String countryName, String city) {
        locationInteractor.saveCountryNameCity(countryName, city);

        disposables.add(locationInteractor.checkCanSearch()
                .andThen(searchInteractor.checkCanSearch())
                .doOnComplete(() -> {
                    setSearchDone(true);
                    searchInteractor.searchStations();
                })
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    public void refreshSearch() {
        disposables.add(searchInteractor.checkCanSearch()
                .doOnComplete(() -> {
                    setSearchDone(true);
                    view.resetSearchResult();
                    searchInteractor.refreshStations();
                })
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    private void enableAutodetect(boolean enabled) {
        locationInteractor.saveAutodetect(enabled);
        view.setAutodetect(enabled);
        if (!enabled) newSearch();
    }

    public void newSearch() {
        searchInteractor.resetSearch();
        view.resetSearchResult();
        setSearchDone(false);
    }

    private void setSearchDone(boolean isSearchDone) {
        view.setSearchDone(isSearchDone);
        if (!isSearchDone) view.showCity(true);
        if (locationInteractor.isAutodetect()) {
            view.showNewSearchBtn(false);
        } else {
            view.showNewSearchBtn(isSearchDone);
        }
    }
}
