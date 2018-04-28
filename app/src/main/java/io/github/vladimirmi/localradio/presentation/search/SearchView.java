package io.github.vladimirmi.localradio.presentation.search;

import java.util.List;

import io.github.vladimirmi.localradio.data.entity.Country;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public interface SearchView extends BaseView {

    void setCountries(List<Country> countries);

    void setCities(List<String> cities);

    void setCountryName(String name);

    void setCity(String city);

    void setAutodetect(boolean enabled);

    void setAutoSearchDone(boolean done);

    void setManualSearchDone(boolean done);

    void setSearchResult(int foundStations);

    void resetSearchResult();

    void setSearching(boolean enabled);
}
