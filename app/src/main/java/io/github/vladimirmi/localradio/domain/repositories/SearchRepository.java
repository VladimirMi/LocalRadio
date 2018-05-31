package io.github.vladimirmi.localradio.domain.repositories;

import android.util.Pair;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.SearchState;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 28.05.2018.
 */
public interface SearchRepository {

    Observable<SearchState> searchState();

    void setSearchState(SearchState state);

    void setSkipCache(boolean skipCache);

    Single<List<Station>> searchStationsManual(String countryCode, String city);

    Single<List<Station>> searchStationsByCoordinates(Pair<Float, Float> coordinates);

    Single<List<Station>> searchStationsByIp();
}
