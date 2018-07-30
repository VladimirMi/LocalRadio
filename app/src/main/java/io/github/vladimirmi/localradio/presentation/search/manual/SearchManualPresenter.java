package io.github.vladimirmi.localradio.presentation.search.manual;

import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchManualPresenter extends BasePresenter<SearchManualView> {

    private final LocationInteractor locationInteractor;
    private final SearchInteractor searchInteractor;

    @Inject
    SearchManualPresenter(LocationInteractor locationInteractor,
                          SearchInteractor searchInteractor) {
        this.locationInteractor = locationInteractor;
        this.searchInteractor = searchInteractor;
    }

    @Override
    protected void onFirstAttach(SearchManualView view, CompositeDisposable dataSubs) {
        setCountrySuggestions();
        setCitySuggestions("");
        dataSubs.add(locationInteractor.getSavedLocation()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<LocationEntity>(view) {
                    @Override
                    public void onSuccess(LocationEntity locationEntity) {
                        setLocation(locationEntity);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof NoSuchElementException) {
                            setLocation(null);
                        } else {
                            super.onError(e);
                        }
                    }
                }));
    }

    @Override
    protected void onAttach(SearchManualView view) {
//        viewSubs.add(searchInteractor.getSearchResultObs()
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(new RxUtils.ErrorObserver<SearchResult>(view) {
//                    @Override
//                    public void onNext(SearchResult result) {
//                        handleSearchResult(result);
//                    }
//                }));
    }

    public void selectCountry(LocationEntity location) {
        if (location == null) {
            setCitySuggestions("");
        }
        LocationEntity savedLocation = locationInteractor.saveCountryLocation(location);
        setLocation(savedLocation);
    }

    public void selectCity(LocationEntity location) {
        LocationEntity savedLocation = locationInteractor.saveCityLocation(location);
        setLocation(savedLocation);
    }

    private void setLocation(LocationEntity location) {
        if (location == null) {
            view.setCity("");
            view.setCountry("");
            setCitySuggestions("");
            view.setStationsNumber(0);
        } else {
            if (location.isCountry()) {
                view.setCountry(location.name);
                view.setCity("");
                setCitySuggestions(location.country);
            } else {
                setCountry(location.country);
                view.setCity(location.name);
            }
            view.setStationsNumber(location.stations);
        }
    }

    private void setCountrySuggestions() {
        viewSubs.add(locationInteractor.getCountries()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<List<LocationEntity>>(view) {
                    @Override
                    public void onSuccess(List<LocationEntity> locations) {
                        view.setCountrySuggestions(locations);
                    }
                }));
    }

    private void setCitySuggestions(String countryCode) {
        viewSubs.add(locationInteractor.getCities(countryCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<List<LocationEntity>>(view) {
                    @Override
                    public void onSuccess(List<LocationEntity> locations) {
                        view.setCitySuggestions(locations);
                    }
                }));
    }

    private void setCountry(String countryCode) {
        viewSubs.add(locationInteractor.getCountry(countryCode)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<LocationEntity>(view) {
                    @Override
                    public void onSuccess(LocationEntity location) {
                        view.setCountry(location.name);
                    }
                }));
    }

//    public void enableAutodetect(boolean autodetect) {
//        if (!autodetect) {
//            searchInteractor.resetSearch();
//            newSearchState();
//        } else {
//            dataSubs.add(view.resolvePermissions(Manifest.permission.ACCESS_FINE_LOCATION)
//                    .doOnNext(enabled -> {
//                        // TODO: 4/27/18 add action that opens settings to the snackbar
//                        setAutodetect(enabled);
//                        if (!enabled) view.showMessage(R.string.need_permission);
//                    })
//                    .filter(enabled -> enabled)
//                    .flatMapCompletable(enabled -> searchInteractor.searchStations())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribeWith(new RxUtils.ErrorCompletableObserver(getView())));
//        }
//    }
//
//
//    public void refreshSearch() {
//        dataSubs.add(searchInteractor.refreshStations()
//                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView())));
//    }
//
//    public void search(String countryName, String city) {
//        if (!searchInteractor.getSearchState().isSearchDone()) {
//            performSearch(countryName, city);
//        } else {
//            searchInteractor.resetSearch();
//            newSearchState();
//        }
//    }

//    private void performSearch(String countryName, String city) {
//        if (countryName.isEmpty()) {
//            countryName = locationInteractor.findCountryName(city);
//            selectCountry(countryName);
//        }
//        locationInteractor.saveCountryNameCity(countryName, city);
//
//        dataSubs.add(locationInteractor.checkCanSearch()
//                .andThen(searchInteractor.searchStations())
//                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView())));
//    }
//
//    private void handleSearchResult(SearchResult searchResult) {
//        switch (searchResult.state) {
//            case NOT_DONE:
//                newSearchState();
//                break;
//            case LOADING:
//                loadingState();
//                break;
//            case AUTO_DONE:
//            case MANUAL_DONE:
//                view.setSearchResult(searchResult.message);
//                selectCountry(locationInteractor.getCountryName());
//                view.setCity(locationInteractor.getCity());
//
//                if (searchResult.state == SearchResult.State.AUTO_DONE) {
//                    searchDoneAutoState();
//                } else {
//                    searchDoneManualState();
//                }
//                break;
//        }
//    }
//
//    private void setAutodetect(boolean enabled) {
//        locationInteractor.saveAutodetect(enabled);
//        view.setAutodetect(enabled);
//    }
//
//    private void newSearchState() {
//        view.resetSearchResult();
//        view.setSearchDone(false);
//        view.showSearchBtn(true);
//        setAutodetect(false);
//    }
//
//    private void loadingState() {
//        view.resetSearchResult();
//        view.setSearching(true);
//        view.enableAutodetect(false);
//        view.setSearchDone(true);
//        view.showSearchBtn(false);
//    }
//
//    private void searchDoneAutoState() {
//        view.setSearching(false);
//        view.enableAutodetect(locationInteractor.isServicesAvailable());
//        view.setSearchDone(true);
//        view.showSearchBtn(false);
//        view.setAutodetect(true);
//    }
//
//    private void searchDoneManualState() {
//        view.setSearching(false);
//        view.enableAutodetect(locationInteractor.isServicesAvailable());
//        view.setSearchDone(true);
//        view.showSearchBtn(true);
//    }
}
