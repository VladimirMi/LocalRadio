package io.github.vladimirmi.localradio.presentation.search.map;

import com.google.android.gms.maps.model.CameraPosition;

import java.util.Set;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.map.MapPosition;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public interface SearchMapView extends BaseView {

    void initOptions(String mapMode);

    void changeRadius(CameraPosition cameraPosition);

    void restoreMapPosition(MapPosition position, boolean animate);

    void addClusters(Set<LocationClusterItem> clusterItems);

    void setMapMode(String mode);

    void selectClusters(Set<LocationClusterItem> clusterItems);

    void enableLocationData(boolean enabled);
}
