package io.github.vladimirmi.localradio.presentation.search.manual;

import java.util.List;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public interface SearchManualView extends BaseView {

    void setCountrySuggestions(List<LocationEntity> countries);

    void setCitySuggestions(List<LocationEntity> cities);

    void setCountry(String name);

    void setCity(String city);

    void setStationsNumber(int stations);
}
