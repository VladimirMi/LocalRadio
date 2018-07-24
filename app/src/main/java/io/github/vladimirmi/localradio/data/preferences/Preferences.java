package io.github.vladimirmi.localradio.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.presentation.search.SearchPresenter;
import io.github.vladimirmi.localradio.presentation.stations.StationsPagerFragment;

public class Preferences {

    private static final String KEY_AUTODETECT = "AUTODETECT";
    public final Preference<Boolean> autodetect;

    private static final String KEY_COUNTRY_CODE = "COUNTRY_CODE";
    public final Preference<String> countryCode;

    private static final String KEY_CITY = "CITY";
    public final Preference<String> city;

    private static final String KEY_SEARCH_DONE = "SEARCH_DONE";
    public final Preference<Boolean> isSearchDone;

    private static final String KEY_CURRENT_STATION_ID = "CURRENT_STATION_ID";
    public final Preference<Integer> currentStationId;

    private static final String KEY_CURRENT_STATION_IS_FAVORITE = "CURRENT_STATION_IS_FAVORITE";
    public final Preference<Boolean> currentStationIsFavorite;

    private static final String KEY_PAGE = "PAGE";
    public final Preference<Integer> pagePosition;

    private static final String KEY_SEARCH_MODE = "SEARCH_MODE";
    public final Preference<Integer> searchMode;

    private static final String KEY_MAP_MODE = "MAP_MODE";
    public final Preference<String> mapMode;

    private static final String KEY_MAP_LAT = "MAP_LAT";
    public final Preference<Float> mapLat;

    private static final String KEY_MAP_LONG = "MAP_LONG";
    public final Preference<Float> mapLong;

    private static final String KEY_MAP_ZOOM = "MAP_ZOOM";
    public final Preference<Float> mapZoom;

    @Inject
    public Preferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("default", Context.MODE_PRIVATE);

        autodetect = new Preference<>(prefs, KEY_AUTODETECT, false);
        countryCode = new Preference<>(prefs, KEY_COUNTRY_CODE, "");
        city = new Preference<>(prefs, KEY_CITY, "");
        isSearchDone = new Preference<>(prefs, KEY_SEARCH_DONE, false);
        currentStationId = new Preference<>(prefs, KEY_CURRENT_STATION_ID, 0);
        currentStationIsFavorite = new Preference<>(prefs, KEY_CURRENT_STATION_IS_FAVORITE, false);
        pagePosition = new Preference<>(prefs, KEY_PAGE, StationsPagerFragment.PAGE_STATIONS);
        searchMode = new Preference<>(prefs, KEY_SEARCH_MODE, SearchPresenter.MAP_MODE);
        mapMode = new Preference<>(prefs, KEY_MAP_MODE, MapWrapper.COUNTRY_MODE);
        mapLat = new Preference<>(prefs, KEY_MAP_LAT, 0f);
        mapLong = new Preference<>(prefs, KEY_MAP_LONG, 0f);
        mapZoom = new Preference<>(prefs, KEY_MAP_ZOOM, 0f);
    }
}
