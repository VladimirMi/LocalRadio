package io.github.vladimirmi.localradio.presentation.search.map;

import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import butterknife.BindView;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.github.vladimirmi.localradio.presentation.core.BaseMapFragment;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapFragment extends BaseMapFragment<SearchMapPresenter> implements SearchMapView {

    @BindView(R.id.mapView) MapView mapView;
    @BindView(R.id.currentLocRBtn) RadioButton currentLocRBtn;
    @BindView(R.id.mile50RBtn) RadioButton mile50RBtn;
    @BindView(R.id.countryRBtn) RadioButton countryRBtn;
    @BindView(R.id.selectionRg) RadioGroup selectionRg;
    @BindView(R.id.selectionResultTv) TextView selectionResultTv;
    private ClusterManager<LocationCluster> clusterManager;

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
        //noinspection ConstantConditions
        clusterManager = new ClusterManager<>(getContext(), map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        presenter.onMapReady();
    }

    //region =============== SearchMapView ==============

    @Override
    public void setClusterItems(List<LocationCluster> clusters) {
        clusterManager.addItems(clusters);
    }

    //endregion
}
