package io.github.vladimirmi.localradio.presentation.search.map;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapPresenter extends BasePresenter<SearchMapView> {

    public static final String EXACT_MODE = "EXACT_MODE";
    public static final String RADIUS_MODE = "RADIUS_MODE";
    public static final String COUNTRY_MODE = "COUNTRY_MODE";

    private final LocationInteractor locationInteractor;

    @Inject
    public SearchMapPresenter(LocationInteractor locationInteractor) {
        this.locationInteractor = locationInteractor;
    }

    @Override
    protected void onFirstAttach(SearchMapView view, CompositeDisposable disposables) {
        initOptions();
    }

    public void initOptions() {
        view.initOptions(locationInteractor.getMapMode());
    }

    public void onMapReady() {
        initMapMode();
    }

    public void selectCountry() {
        locationInteractor.saveMapMode(COUNTRY_MODE);
        initMapMode();
    }

    public void selectRadius() {
        locationInteractor.saveMapMode(RADIUS_MODE);
        initMapMode();
    }

    public void selectExact() {
        locationInteractor.saveMapMode(EXACT_MODE);
        initMapMode();
    }

    private void initMapMode() {
        switch (locationInteractor.getMapMode()) {
            case EXACT_MODE:
                setExactMode();
                break;
            case RADIUS_MODE:
                setRadiusMode();
                break;
            case COUNTRY_MODE:
                setCountryMode();
        }
    }

    private void setExactMode() {
        viewSubs.add(locationInteractor.getCityClusters()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> view.setExactMode(list)));
    }

    private void setRadiusMode() {
        viewSubs.add(locationInteractor.getCityClusters()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> view.setRadiusMode(list)));
    }

    private void setCountryMode() {
        viewSubs.add(locationInteractor.getCountryClusters()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> view.setCountryMode(list)));
    }
}
