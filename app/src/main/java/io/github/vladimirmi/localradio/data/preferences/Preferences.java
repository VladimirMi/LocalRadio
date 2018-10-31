package io.github.vladimirmi.localradio.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.presentation.search.SearchPresenter;
import io.github.vladimirmi.localradio.presentation.stations.StationsPagerFragment;

public class Preferences {

    public static final String NAME = "default";

    private static final String KEY_LOCATIONS = "LOCATIONS";
    public final Preference<Set<String>> locations;

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

    // TODO: 27.10.18 remove zoom
    private static final String KEY_MAP_ZOOM = "MAP_ZOOM";
    public final Preference<Float> mapZoom;

    private static final String KEY_INITIAL_BUFFER_LENGTH = "INITIAL_BUFFER_LENGTH";
    public final Preference<Integer> initialBufferLength;

    private static final String KEY_BUFFER_LENGTH = "BUFFER_LENGTH";
    public final Preference<Integer> bufferLength;

    @Inject
    public Preferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);

        locations = new Preference<>(prefs, KEY_LOCATIONS, Collections.emptySet());
        isSearchDone = new Preference<>(prefs, KEY_SEARCH_DONE, false);
        currentStationId = new Preference<>(prefs, KEY_CURRENT_STATION_ID, 0);
        currentStationIsFavorite = new Preference<>(prefs, KEY_CURRENT_STATION_IS_FAVORITE, false);
        pagePosition = new Preference<>(prefs, KEY_PAGE, StationsPagerFragment.PAGE_STATIONS);
        searchMode = new Preference<>(prefs, KEY_SEARCH_MODE, SearchPresenter.MANUAL_MODE);
        mapMode = new Preference<>(prefs, KEY_MAP_MODE, MapWrapper.COUNTRY_MODE);
        mapLat = new Preference<>(prefs, KEY_MAP_LAT, 0f);
        mapLong = new Preference<>(prefs, KEY_MAP_LONG, 0f);
        mapZoom = new Preference<>(prefs, KEY_MAP_ZOOM, 0f);
        initialBufferLength = new Preference<>(prefs, KEY_INITIAL_BUFFER_LENGTH, 3);
        bufferLength = new Preference<>(prefs, KEY_BUFFER_LENGTH, 6);
    }
}
