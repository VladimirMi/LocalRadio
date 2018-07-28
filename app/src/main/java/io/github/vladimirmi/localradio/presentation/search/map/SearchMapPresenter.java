package io.github.vladimirmi.localradio.presentation.search.map;

import android.arch.persistence.db.SupportSQLiteQuery;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.CustomClusterManager;
import io.github.vladimirmi.localradio.map.MapState;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapPresenter extends BasePresenter<SearchMapView> {

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
        view.restoreMapState(locationInteractor.getMapState());
    }

    public void loadClusters(Observable<SupportSQLiteQuery> queryObservable) {
        viewSubs.add(queryObservable
                .flatMapSingle(locationInteractor::loadClusters)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::addClusters));
    }

    public void radiusZoomChange(Observable<Float> radiusZoomObservable) {
        viewSubs.add(radiusZoomObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::changeRadius));
    }

    public void selectedItemsChange(Observable<List<LocationClusterItem>> selectedItemsObservable) {
        viewSubs.add(selectedItemsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locationClusterItems -> {
                    int c = 0;
                    for (LocationClusterItem locationClusterItem : locationClusterItems) {
                        c += locationClusterItem.getStationsNum();
                    }
                }));
    }

    public void selectCountry() {
        locationInteractor.saveMapMode(CustomClusterManager.COUNTRY_MODE);
        initMapMode();
    }

    public void selectRadius() {
        locationInteractor.saveMapMode(CustomClusterManager.RADIUS_MODE);
        initMapMode();
    }

    public void selectExact() {
        locationInteractor.saveMapMode(CustomClusterManager.EXACT_MODE);
        initMapMode();
    }

    private void initMapMode() {
        view.setMapMode(locationInteractor.getMapMode());
        switch (locationInteractor.getMapMode()) {
            case CustomClusterManager.EXACT_MODE:
                view.setExactMode();
                break;
            case CustomClusterManager.RADIUS_MODE:
                view.setRadiusMode();
                break;
            case CustomClusterManager.COUNTRY_MODE:
                view.setCountryMode();
        }
    }

    public void saveMapState(MapState state) {
        locationInteractor.saveMapState(state);
    }
}
