package io.github.vladimirmi.localradio.presentation.search.manual;

import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchManualPresenter extends BasePresenter<SearchManualView> {

    private final LocationInteractor locationInteractor;
    private LocationEntity cityLocation;
    private LocationEntity countryLocation;

    @Inject
    SearchManualPresenter(LocationInteractor locationInteractor) {
        this.locationInteractor = locationInteractor;
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

    public void selectCountry(LocationEntity location) {
        if (!(location != null && cityLocation != null && location.country.equals(cityLocation.country)) &&
                (location != null || cityLocation == null)) {

            cityLocation = null;
            setLocation(location);
        }
    }

    public void selectCity(LocationEntity location) {
        setLocation(location == null ? countryLocation : location);
    }

    private void setLocation(LocationEntity location) {
        locationInteractor.setSelectedManualLocation(location);
        if (!hasView()) return;
        if (location == null) {
            view.setCity("");
            view.setCountry("");
            setCitySuggestions("");
            view.setSelectionResult(0);
            countryLocation = null;
            cityLocation = null;
        } else {
            setCitySuggestions(location.country);
            if (location.isCountry()) {
                view.setCountry(location.name);
                view.setCity("");
                countryLocation = location;
            } else {
                setCountry(location.country);
                view.setCity(location.name);
                cityLocation = location;
            }
            view.setSelectionResult(location.stations);
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
                        countryLocation = location;
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
}
