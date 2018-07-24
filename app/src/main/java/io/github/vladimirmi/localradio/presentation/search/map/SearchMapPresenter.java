package io.github.vladimirmi.localradio.presentation.search.map;

import com.google.android.gms.maps.GoogleMap;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.interactors.MapInteractor;
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
    private final MapInteractor mapInteractor;
    private String mapMode;

    @Inject
    public SearchMapPresenter(LocationInteractor locationInteractor, MapInteractor mapInteractor) {
        this.locationInteractor = locationInteractor;
        this.mapInteractor = mapInteractor;
    }

    @Override
    protected void onFirstAttach(SearchMapView view, CompositeDisposable disposables) {
        initOptions();
    }

    public void initOptions() {
        view.initOptions(mapInteractor.getMapMode());
    }

    public void onMapReady(GoogleMap map) {
        initMapMode();
        mapInteractor.onMapReady(map);

        viewSubs.add(mapInteractor.getQueryObservable()
                .flatMapSingle(locationInteractor::loadClusters)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mapInteractor::addClusters));

        viewSubs.add(mapInteractor.getZoomObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::changeRadius));
    }

    public void selectCountry() {
        mapInteractor.saveMapMode(COUNTRY_MODE);
        initMapMode();
    }

    public void selectRadius() {
        mapInteractor.saveMapMode(RADIUS_MODE);
        initMapMode();
    }

    public void selectExact() {
        mapInteractor.saveMapMode(EXACT_MODE);
        initMapMode();
    }

    private void initMapMode() {
        mapMode = mapInteractor.getMapMode();
        switch (mapMode) {
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
}
