package io.github.vladimirmi.localradio.presentation.search.map;

import android.Manifest;
import android.util.Pair;

import com.google.android.gms.maps.model.CameraPosition;
import com.tbruyelle.rxpermissions2.Permission;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
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
    protected void onDestroy() {
        if (radiusSub != null) radiusSub.dispose();
    }

    public void initOptions() {
        view.initOptions(locationInteractor.getMapMode());
    }

    public void onMapReady() {
        view.setMapMode(locationInteractor.getMapMode());
        view.restoreMapPosition(locationInteractor.getPosition(), false);
        loadClusters();

        viewSubs.add(locationInteractor.getMapLocations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::selectClusters));
    }

    private void loadClusters() {
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

    public void setMapPosition(MapPosition position) {
        locationInteractor.setMapPosition(position);
    }


    public void askLocationPermission() {
        dataSubs.add(view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribeWith(new RxUtils.ErrorObserver<Permission>(view) {
                    @Override
                    public void onNext(Permission permission) {
                        view.enableLocationData(permission.granted);
                    }
                }));
    }

    public void findMyLocation() {
        viewSubs.add(locationInteractor.checkCanGetLocation()
                .andThen(locationInteractor.getMyLocation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorSingleObserver<Pair<MapPosition, LocationClusterItem>>(view) {
                    @Override
                    public void onSuccess(Pair<MapPosition, LocationClusterItem> pair) {
                        view.restoreMapPosition(pair.first, true);
                        view.selectClusters(Collections.singleton(pair.second));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof TimeoutException)
                            view.showMessage(R.string.error_get_location);
                        else super.onError(e);
                    }
                })
        );
    }
}
