package io.github.vladimirmi.localradio.presentation.search.map;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.Set;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.custom.RadiusView;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.MapPosition;
import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.presentation.core.BaseMapFragment;
import io.github.vladimirmi.localradio.utils.UiUtils;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapFragment extends BaseMapFragment<SearchMapPresenter> implements SearchMapView {

    @BindView(R.id.mapView) MapView mapView;
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
        return Scopes.getLocationsScope().getInstance(SearchMapPresenter.class);
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
        presenter.onMapReady();
        mapWrapper.setOnSaveMapPositionListener(position -> presenter.setMapPosition(position));
        presenter.selectedItemsChange(mapWrapper.getSelectedItemsObservable());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getView() != null && isVisibleToUser) {
            presenter.initOptions();
            //noinspection ConstantConditions
            UiUtils.hideSoftKeyBoard(getContext(), selectionRg.getWindowToken());
        }
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
                presenter.setMapMode(MapWrapper.COUNTRY_MODE);
            } else if (checkedId == R.id.radiusRBtn) {
                presenter.setMapMode(MapWrapper.RADIUS_MODE);
            } else {
                presenter.setMapMode(MapWrapper.EXACT_MODE);
            }
        });
    }

    @Override
    public void setMapMode(String mode) {
        mapWrapper.setMapMode(mode);
        setupRadius(mode);
    }

    @Override
    public void changeRadius(CameraPosition cameraPosition) {
        radiusView.setCameraPosition(cameraPosition);
    }

    @Override
    public void restoreMapPosition(MapPosition position) {
        mapWrapper.restoreMapPosition(position);
    }

    @Override
    public void addClusters(Set<LocationClusterItem> clusterItems) {
        mapWrapper.addClusters(clusterItems);
    }

    @Override
    public void selectClusters(Set<LocationClusterItem> clusterItems) {
        int stations = 0;
        for (LocationClusterItem locationClusterItem : clusterItems) {
            stations += locationClusterItem.getStationsNum();
        }
        setSelectionResult(stations);
        mapWrapper.selectClusters(clusterItems);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void enableLocationData(boolean enabled) {
        GoogleMap map = mapWrapper.getMap();
        map.setMyLocationEnabled(enabled);
        map.setOnMyLocationButtonClickListener(() -> {
            presenter.animateMyLocation = true;
            return false;
        });
    }

    //endregion

    private void setSelectionResult(int stations) {
        String s = getResources().getQuantityString(R.plurals.selection_result, stations, stations);
        selectionResultTv.setText(s);
    }

    private void setupRadius(String mode) {
        if (MapWrapper.RADIUS_MODE.equals(mode)) {
            radiusView.setVisibility(View.VISIBLE);
            presenter.selectRadiusChange(mapWrapper.getRadiusChangeObservable());
        } else {
            radiusView.setVisibility(View.GONE);
        }
    }
}
