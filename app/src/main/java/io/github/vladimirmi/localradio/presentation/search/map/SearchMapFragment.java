package io.github.vladimirmi.localradio.presentation.search.map;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.github.vladimirmi.localradio.presentation.core.BaseMapFragment;
import io.github.vladimirmi.localradio.utils.MapHelper;

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
        //noinspection ConstantConditions
        clusterManager = new ClusterManager<>(getContext(), map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        presenter.onMapReady();
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
    public void setExactMode(List<LocationCluster> clusters) {
        clearMap();
        map.setMinZoomPreference(4f);
        clusterManager.addItems(clusters);
    }

    @Override
    public void setRadiusMode(List<LocationCluster> clusters) {
        clearMap();
        map.setMinZoomPreference(4f);
        clusterManager.addItems(clusters);
    }

    @Override
    public void setCountryMode(List<LocationEntity> countries) {
        clearMap();
        map.setMinZoomPreference(3f);
        map.setMaxZoomPreference(7f);
        MapHelper helper = new MapHelper(getContext());
        for (LocationEntity country : countries) {
            map.addMarker(helper.createCountryMarker(country));
        }
    }

    //endregion

    private void clearMap() {
        clusterManager.clearItems();
        map.clear();
    }
}
