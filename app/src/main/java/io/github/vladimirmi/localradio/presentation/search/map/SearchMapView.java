package io.github.vladimirmi.localradio.presentation.search.map;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public interface SearchMapView extends BaseView {

    void initOptions(String mapMode);

    void setExactMode();

    void setRadiusMode();

    void setCountryMode();

    void changeRadius(Float zoom);

    void setClusters(List<LocationClusterItem> clusters);
}
