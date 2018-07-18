package io.github.vladimirmi.localradio.presentation.search.map;

import com.google.android.gms.maps.model.LatLngBounds;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.Observable;
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

    public void onMapMove(Observable<LatLngBounds> cameraMove) {
        viewSubs.add(cameraMove
                .flatMapSingle(bound -> {
                    if (locationInteractor.getMapMode().equals(COUNTRY_MODE)) {
                        return locationInteractor.getCountryClusters(bound);
                    } else {
                        return locationInteractor.getCityClusters(bound);
                    }
                })
                .subscribe(clusters -> {
                    if (locationInteractor.getMapMode().equals(RADIUS_MODE)) {
                        view.setRadius();
                    }
                    view.setClusters(clusters);
                }));
    }

    private void initMapMode() {
        switch (locationInteractor.getMapMode()) {
            case EXACT_MODE:
                view.setExactMode();
                break;
            case RADIUS_MODE:
                view.setRadiusMode();
                break;
            case COUNTRY_MODE:
                view.setCountryMode();
        }
    }

//    private void setExactMode() {
//        viewSubs.add(locationInteractor.getCityClusters(bound)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(list -> view.setExactMode(list)));
//    }
//
//    private void setRadiusMode() {
//        viewSubs.add(locationInteractor.getCityClusters(bound)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(list -> view.setRadiusMode(list)));
//    }
//
//    private void setCountryMode() {
//        viewSubs.add(locationInteractor.getCountryClusters(bound)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(list -> view.setCountryMode(list)));
//    }
}
