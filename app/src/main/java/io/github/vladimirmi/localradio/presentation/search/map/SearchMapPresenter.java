package io.github.vladimirmi.localradio.presentation.search.map;

import android.arch.persistence.db.SupportSQLiteQuery;

import com.google.android.gms.maps.model.CameraPosition;

import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.MapState;
import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapPresenter extends BasePresenter<SearchMapView> {

    private final LocationInteractor locationInteractor;
    private Disposable radiusSub;

    @Inject
    public SearchMapPresenter(LocationInteractor locationInteractor) {
        this.locationInteractor = locationInteractor;
    }

    @Override
    protected void onFirstAttach(SearchMapView view, CompositeDisposable disposables) {
        initOptions();
    }

    @Override
    protected void onDetach() {
        if (radiusSub != null) radiusSub.dispose();
    }

    public void initOptions() {
        view.initOptions(locationInteractor.getMapMode());
    }

    public void onMapReady() {
        initMapMode();
        view.restoreMapState(locationInteractor.getMapState());
        viewSubs.add(locationInteractor.getSavedLocations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::selectClusters));
    }

    public void loadClusters(Observable<SupportSQLiteQuery> queryObservable) {
        viewSubs.add(queryObservable
                .flatMapSingle(locationInteractor::loadClusters)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::addClusters));
    }

    public void selectRadiusChange(Observable<CameraPosition> radiusZoomObservable) {
        if (radiusSub != null) radiusSub.dispose();
        radiusSub = radiusZoomObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::changeRadius);
    }

    public void selectedItemsChange(Observable<Set<LocationClusterItem>> selectedItemsObservable) {
        viewSubs.add(selectedItemsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locationClusterItems -> {
                    int stations = 0;
                    for (LocationClusterItem locationClusterItem : locationClusterItems) {
                        stations += locationClusterItem.getStationsNum();
                    }
                    view.setSelectionResult(stations);
                    locationInteractor.saveLocations(locationClusterItems);
                }));
    }

    public void selectCountry() {
        locationInteractor.saveMapMode(MapWrapper.COUNTRY_MODE);
        initMapMode();
    }

    public void selectRadius() {
        locationInteractor.saveMapMode(MapWrapper.RADIUS_MODE);
        initMapMode();
    }

    public void selectExact() {
        locationInteractor.saveMapMode(MapWrapper.EXACT_MODE);
        initMapMode();
    }

    private void initMapMode() {
        view.setMapMode(locationInteractor.getMapMode());
    }

    public void saveMapState(MapState state) {
        locationInteractor.saveMapState(state);
    }
}
