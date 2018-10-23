package io.github.vladimirmi.localradio.presentation.search.map;

import android.Manifest;

import com.google.android.gms.maps.model.CameraPosition;
import com.tbruyelle.rxpermissions2.Permission;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import androidx.sqlite.db.SupportSQLiteQuery;
import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.MapPosition;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapPresenter extends BasePresenter<SearchMapView> {

    private final LocationInteractor locationInteractor;
    private Disposable radiusSub;
    boolean animateMyLocation = false;

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
        view.setMapMode(locationInteractor.getMapMode());
        view.restoreMapPosition(locationInteractor.getPosition());
        viewSubs.add(locationInteractor.getMapLocations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::selectClusters));
        askLocationPermission();
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
                    view.selectClusters(locationClusterItems);
                    locationInteractor.setSelectedMapLocations(locationClusterItems);
                }));
    }

    public void setMapMode(String mode) {
        locationInteractor.setMapMode(mode);
        view.setMapMode(mode);
    }

    public void setMapPosition(MapPosition position) { // calls on map idle
        if (animateMyLocation) {
            setupMyLocation(position);
        }
        locationInteractor.setMapPosition(position);
    }

    private void setupMyLocation(MapPosition state) {
        view.setMapMode(locationInteractor.getMapMode());
        viewSubs.add(locationInteractor.setMyLocation(state)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locationClusterItem -> {
                    view.selectClusters(Collections.singleton(locationClusterItem));
                }));
        animateMyLocation = false;
    }

    private void askLocationPermission() {
        dataSubs.add(view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribeWith(new RxUtils.ErrorObserver<Permission>(view) {
                    @Override
                    public void onNext(Permission permission) {
                        view.enableLocationData(permission.granted);
                    }
                }));
    }
}
