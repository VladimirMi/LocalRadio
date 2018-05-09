package io.github.vladimirmi.localradio.presentation.search;

import java.util.List;

import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public interface SearchView extends BaseView {

    void setCountrySuggestions(List<String> countries);

    void setCitySuggestions(List<String> cities);

    void setCountryName(String name);

    void setCity(String city);

    void setAutodetect(boolean enabled);

    void setSearchDone(boolean done);

    void showNewSearchBtn(boolean visible);

    void showCity(boolean visible);

    void setSearchResult(int foundStations);

    void resetSearchResult();

    void setSearching(boolean enabled);
}
