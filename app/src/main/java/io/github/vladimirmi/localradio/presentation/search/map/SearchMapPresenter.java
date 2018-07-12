package io.github.vladimirmi.localradio.presentation.search.map;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
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
                view.setExactMode(locationInteractor.getCityClusters());
                break;
            case RADIUS_MODE:
                view.setRadiusMode(locationInteractor.getCityClusters());
                break;
            case COUNTRY_MODE:
                view.setCountryMode(locationInteractor.getCountryClusters());
        }
    }
}
