package io.github.vladimirmi.localradio.data;

import com.squareup.moshi.Json;

import java.util.List;
import java.util.Locale;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class Country {

    @Json(name = "isoCode") private String isoCode;
    @Json(name = "cities") private List<String> cities;

    public String getIsoCode() {
        return isoCode;
    }

    public List<String> getCities() {
        return cities;
    }

    public String getName() {
        return new Locale("", isoCode).getDisplayCountry();
    }
}
