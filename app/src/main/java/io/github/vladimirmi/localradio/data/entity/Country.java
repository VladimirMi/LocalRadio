package io.github.vladimirmi.localradio.data.entity;

import android.content.Context;
import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class Country implements Comparable<Country> {

    @Json(name = "isoCode") private String isoCode;
    @Json(name = "cities") private List<String> cities;
    transient private String name;

    public Country(String isoCode, List<String> cities, String name) {
        this.isoCode = isoCode;
        this.cities = cities;
        this.name = name;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public List<String> getCities() {
        return cities;
    }

    public String getName() {
        if (name == null) {
            name = new Locale("", isoCode).getDisplayCountry();
        }
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(@NonNull Country o) {
        return getName().compareToIgnoreCase(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Country country = (Country) o;

        return name.equals(country.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    public static Country any(Context context) {
        String anyCountry = context.getString(R.string.any_country);
        String anyCity = context.getString(R.string.any_city);
        return new Country("", Collections.singletonList(anyCity), anyCountry);
    }
}
