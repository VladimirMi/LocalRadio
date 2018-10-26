package io.github.vladimirmi.localradio.presentation.search.map;

import android.Manifest;

import com.google.android.gms.maps.model.CameraPosition;
import com.tbruyelle.rxpermissions2.Permission;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.MapPosition;
import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

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
    protected void onDestroy() {
        if (radiusSub != null) radiusSub.dispose();
    }

    public void initOptions() {
        view.initOptions(locationInteractor.getMapMode());
    }

    public void onMapReady() {
        view.setMapMode(locationInteractor.getMapMode());
        view.restoreMapPosition(locationInteractor.getPosition());
        loadClusters();

        viewSubs.add(locationInteractor.getMapLocations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::selectClusters));
        askLocationPermission();
    }

    private void loadClusters() {
        Timber.e("loadClusters: ");
        viewSubs.add(locationInteractor.loadClusters()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::addClusters));
    }

    public void selectRadiusChange(Observable<CameraPosition> radiusZoomObservable) {
        if (radiusSub != null) return;
        radiusSub = radiusZoomObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cameraPosition -> {
                    if (hasView()) view.changeRadius(cameraPosition);
                });
    }

    public void selectedItemsChange(Observable<Set<LocationClusterItem>> selectedItemsObservable) {
        dataSubs.add(selectedItemsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(locationClusterItems -> {
                    if (hasView()) view.selectClusters(locationClusterItems);
                    locationInteractor.setSelectedMapLocations(locationClusterItems);
                }));
    }

    public void setMapMode(String mode) {
        boolean previousIsCountry = locationInteractor.getMapMode().equals(MapWrapper.COUNTRY_MODE);
        boolean currIsCountry = mode.equals(MapWrapper.COUNTRY_MODE);
        locationInteractor.setMapMode(mode);
        view.setMapMode(mode);
        if (currIsCountry != previousIsCountry) {
            loadClusters();
        }
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
