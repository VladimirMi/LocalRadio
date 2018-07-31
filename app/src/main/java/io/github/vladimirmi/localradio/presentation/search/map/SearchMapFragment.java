package io.github.vladimirmi.localradio.presentation.search.map;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.custom.RadiusView;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.CustomClusterManager;
import io.github.vladimirmi.localradio.map.MapState;
import io.github.vladimirmi.localradio.presentation.core.BaseMapFragment;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapFragment extends BaseMapFragment<SearchMapPresenter> implements SearchMapView {

    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.autodetectCb) CheckedTextView autodetectCb;
    @BindView(R.id.selectionRg) RadioGroup selectionRg;
    @BindView(R.id.selectionResultTv) TextView selectionResultTv;
    @BindView(R.id.radiusView) RadiusView radiusView;

    private CustomClusterManager clusterManager;
    private GoogleMap map;

    @Override
    protected int getLayout() {
        return R.layout.fragment_search_map;
    }

    @Override
    protected SearchMapPresenter providePresenter() {
        return Scopes.getAppScope().getInstance(SearchMapPresenter.class);
    }

    @Override
    protected void setupView(View view) {
    }

    @Override
    protected MapView getMapView() {
        return mapView;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        initMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (map != null) initMap();
    }

    private void initMap() {
        clusterManager = new CustomClusterManager(getContext(), map);
        presenter.onMapReady();
        presenter.loadClusters(clusterManager.getQueryObservable());
        presenter.selectRadiusChange(clusterManager.getCameraPositionObservable());
        presenter.selectedItemsChange(clusterManager.getSelectedItemsObservable());
        clusterManager.setOnSaveStateListener(state -> {
            presenter.saveMapState(state);
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null && isVisibleToUser) presenter.initOptions();
    }

    //region =============== SearchMapView ==============

    @Override
    public void initOptions(String mapMode) {
        if (!getUserVisibleHint()) return;
        switch (mapMode) {
            case CustomClusterManager.EXACT_MODE:
                selectionRg.check(R.id.exactLocRBtn);
                break;
            case CustomClusterManager.RADIUS_MODE:
                selectionRg.check(R.id.radiusRBtn);
                break;
            case CustomClusterManager.COUNTRY_MODE:
                selectionRg.check(R.id.countryRBtn);
        }
        selectionRg.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.countryRBtn) {
                presenter.selectCountry();
            } else if (checkedId == R.id.radiusRBtn) {
                presenter.selectRadius();
            } else {
                presenter.selectExact();
            }
        });
    }

    @Override
    public void setMapMode(String mode) {
        clusterManager.setMapMode(mode);
    }

    @Override
    public void setExactMode() {
        // TODO: 7/24/18 move to wrapper
        clusterManager.map.setMinZoomPreference(6f);
        clusterManager.map.setMaxZoomPreference(9f);
    }

    @Override
    public void setRadiusMode() {
        clusterManager.map.setMinZoomPreference(6f);
        clusterManager.map.setMaxZoomPreference(9f);
    }

    @Override
    public void changeRadius(CameraPosition cameraPosition) {
        radiusView.setCameraPosition(cameraPosition);
    }

    @Override
    public void setCountryMode() {
        clusterManager.map.setMinZoomPreference(2f);
        clusterManager.map.setMaxZoomPreference(7f);
    }

    @Override
    public void restoreMapState(MapState state) {
        clusterManager.restoreMapState(state);
    }

    @Override
    public void addClusters(List<LocationClusterItem> clusterItems) {
        Timber.e("addClusters: ");
        clusterManager.addItems(clusterItems);
    }

    //endregion
}
