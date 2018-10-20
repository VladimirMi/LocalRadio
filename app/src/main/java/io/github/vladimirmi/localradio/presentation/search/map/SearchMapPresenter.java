package io.github.vladimirmi.localradio.presentation.search.map;

import android.Manifest;

import com.google.android.gms.maps.model.CameraPosition;
import com.tbruyelle.rxpermissions2.Permission;

import java.util.Set;

import javax.inject.Inject;

import androidx.sqlite.db.SupportSQLiteQuery;
import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.MapState;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

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
        view.showAutodetect(locationInteractor.isServicesAvailable());
        view.setAutodetect(locationInteractor.isAutodetect());
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
        view.restoreMapState(locationInteractor.getMapState());
        viewSubs.add(locationInteractor.getMapLocations()
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
                    view.selectClusters(locationClusterItems);
                    locationInteractor.setSelectedMapLocations(locationClusterItems);
                }));
    }

    public void setMapMode(String mode) {
        locationInteractor.setMapMode(mode);
        view.setMapMode(mode);
    }

    public void setMapState(MapState state) {
        locationInteractor.setMapState(state);
    }

    public void enableAutodetect(boolean checked) {
        dataSubs.add(view.resolvePermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribeWith(new RxUtils.ErrorObserver<Permission>(view) {
                    @Override
                    public void onNext(Permission permission) {
                        if (permission.granted) {
                            locationInteractor.saveAutodetect(checked);
                            view.setAutodetect(checked);
                        } else {
                            if (!permission.shouldShowRequestPermissionRationale) {
                                // TODO: 20.10.18 goto settings
                            }
                            locationInteractor.saveAutodetect(false);
                            view.setAutodetect(false);
                        }
                        Timber.e("onNext: granted %s, never ask %s", permission.granted, !permission.shouldShowRequestPermissionRationale);
                    }
                }));
    }
}
