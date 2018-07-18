package io.github.vladimirmi.localradio.presentation.search.map;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.custom.CustomClusterRenderer;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.github.vladimirmi.localradio.presentation.core.BaseMapFragment;
import io.github.vladimirmi.localradio.utils.MapUtils;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapFragment extends BaseMapFragment<SearchMapPresenter> implements SearchMapView {

    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.autodetectCb) CheckedTextView autodetectCb;
    @BindView(R.id.selectionRg) RadioGroup selectionRg;
    @BindView(R.id.selectionResultTv) TextView selectionResultTv;

    private ClusterManager<LocationCluster> clusterManager;
    private GoogleMap map;
    private Circle radiusCircle;

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
        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);

        this.map = map;
        //noinspection ConstantConditions
        clusterManager = new ClusterManager<>(getContext(), map);
        clusterManager.setRenderer(new CustomClusterRenderer(getContext(), map, clusterManager));

        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        presenter.onMapReady();
        presenter.onMapMove(MapUtils.observeCameraMove(map));
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
            case SearchMapPresenter.EXACT_MODE:
                selectionRg.check(R.id.exactLocRBtn);
                break;
            case SearchMapPresenter.RADIUS_MODE:
                selectionRg.check(R.id.radiusRBtn);
                break;
            case SearchMapPresenter.COUNTRY_MODE:
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
    public void setExactMode() {
        map.setMinZoomPreference(2f);
        map.setMaxZoomPreference(9f);
    }

    @Override
    public void setRadiusMode() {
        map.setMinZoomPreference(6f);
        map.setMaxZoomPreference(9f);
    }

    @Override
    public void setRadius() {
        if (radiusCircle != null) radiusCircle.remove();
        radiusCircle = map.addCircle(new CircleOptions().center(map.getCameraPosition().target).radius(80000));
    }

    @Override
    public void setCountryMode() {
        map.setMinZoomPreference(2f);
        map.setMaxZoomPreference(7f);
    }

    @Override
    public void setClusters(List<LocationCluster> clusters) {
        clusterManager.clearItems();
        clusterManager.addItems(clusters);
    }

    //endregion

    private void clearMap() {
        clusterManager.clearItems();
        map.clear();
    }
}
