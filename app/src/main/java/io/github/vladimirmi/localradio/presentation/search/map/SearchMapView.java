package io.github.vladimirmi.localradio.presentation.search.map;

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

}
