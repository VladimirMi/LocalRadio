package io.github.vladimirmi.localradio.presentation.search.manual;

import java.util.List;

import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public interface SearchManualView extends BaseView {

    void setCountrySuggestions(List<String> countries);

    void setCitySuggestions(List<String> cities);

    void setCountryName(String name);

    void setCity(String city);

    void setAutodetect(boolean enabled);

    void setSearchDone(boolean done);

    void showSearchBtn(boolean visible);

    void setSearchResult(String result);

    void resetSearchResult();

    void setSearching(boolean enabled);

    void enableAutodetect(boolean enabled);
}
