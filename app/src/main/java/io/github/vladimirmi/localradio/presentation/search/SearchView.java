package io.github.vladimirmi.localradio.presentation.search;

import java.util.List;

import io.github.vladimirmi.localradio.data.Country;
import io.github.vladimirmi.localradio.presentation.core.BaseView;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public interface SearchView extends BaseView {

    void setCountries(List<Country> countries);

    void setCities(List<String> cities);

    void setCountry(String name);

    void setCity(List<String> cities);

    void setAutodetect(Boolean enabled);
}
