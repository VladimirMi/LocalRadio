package io.github.vladimirmi.localradio.presentation.search.manual;

import androidx.annotation.NonNull;

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
        dataSubs.add(locationInteractor.getManualLocation()
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
        if (location == null) {
            if (cityLocation == null) emptyState();

        } else if (cityLocation == null || !location.country.equals(cityLocation.country)) {
            countryState(location);
        }
    }

    public void selectCity(LocationEntity location) {
        if (location == null) {
            if (countryLocation == null) emptyState();
            else countryState(countryLocation);
        } else {
            if (cityLocation != null && cityLocation.id == location.id) return;
            cityState(location);
        }
    }

    private void setLocation(LocationEntity location) {
        if (!hasView()) return;
        if (location == null) {
            emptyState();
        } else {
            if (location.isCountry()) {
                countryState(location);
            } else {
                cityState(location);
            }
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

    private void emptyState() {
        if (!hasView()) return;
        setCitySuggestions("");
        view.setSelectionResult(0);
        locationInteractor.setSelectedManualLocation(null);
    }

    private void countryState(@NonNull LocationEntity location) {
        if (!hasView()) return;
        view.setCountry(location.name);
        view.setCity("");
        countryLocation = location;
        cityLocation = null;
        setCitySuggestions(location.country);
        view.setSelectionResult(location.stations);
        locationInteractor.setSelectedManualLocation(location);
    }

    private void cityState(@NonNull LocationEntity location) {
        if (!hasView()) return;
        setCountry(location.country);
        view.setCity(location.name);
        cityLocation = location;
        setCitySuggestions(location.country);
        view.setSelectionResult(location.stations);
        locationInteractor.setSelectedManualLocation(location);
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
