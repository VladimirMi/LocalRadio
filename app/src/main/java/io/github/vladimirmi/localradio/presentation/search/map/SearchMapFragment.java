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
import io.github.vladimirmi.localradio.map.MapState;
import io.github.vladimirmi.localradio.map.MapWrapper;
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

    private MapWrapper mapWrapper;

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
        mapWrapper = new MapWrapper(getContext(), map);
        initMap();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mapWrapper != null) initMap();
    }

    private void initMap() {
        presenter.onMapReady();
        presenter.loadClusters(mapWrapper.getQueryObservable());
        presenter.selectedItemsChange(mapWrapper.getSelectedItemsObservable());
        mapWrapper.setOnSaveStateListener(state -> {
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
            case MapWrapper.EXACT_MODE:
                selectionRg.check(R.id.exactLocRBtn);
                break;
            case MapWrapper.RADIUS_MODE:
                selectionRg.check(R.id.radiusRBtn);
                break;
            case MapWrapper.COUNTRY_MODE:
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
        Timber.e("setMapMode: " + mode);
        mapWrapper.setMapMode(mode);
        if (mode.equals(MapWrapper.RADIUS_MODE)) {
            radiusView.setVisibility(View.VISIBLE);
            presenter.selectRadiusChange(mapWrapper.getRadiusChangeObservable());
        } else {
            radiusView.setVisibility(View.GONE);
        }
    }

    @Override
    public void changeRadius(CameraPosition cameraPosition) {
        radiusView.setCameraPosition(cameraPosition);
    }

    @Override
    public void restoreMapState(MapState state) {
        mapWrapper.restoreMapState(state);
    }

    @Override
    public void addClusters(List<LocationClusterItem> clusterItems) {
        Timber.e("addClusters: ");
        mapWrapper.addClusters(clusterItems);
    }

    //endregion
}
