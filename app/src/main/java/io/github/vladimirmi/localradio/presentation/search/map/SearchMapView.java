package io.github.vladimirmi.localradio.presentation.search.map;

import com.google.android.gms.maps.model.CameraPosition;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.MapState;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public interface SearchMapView extends BaseView {

    void initOptions(String mapMode);

    void changeRadius(CameraPosition cameraPosition);

    void restoreMapState(MapState state);

    void addClusters(List<LocationClusterItem> clusterItems);

    void setMapMode(String mode);
}
