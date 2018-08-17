package io.github.vladimirmi.localradio.domain.repositories;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.map.MapState;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * null
 * Created by Vladimir Mikhalev 28.05.2018.
 */
public interface SearchRepository {

    Observable<SearchResult> searchResult();

    void setSearchResult(SearchResult state);

    void setSkipCache(boolean skipCache);

    void saveSearchMode(int mode);

    int getSearchMode();

    Single<List<Station>> searchStationsByCoordinates(MapState state);

    Single<List<Station>> searchStationsByCountry(String country);

    Single<List<Station>> searchStationsByCity(String country, String city);
}
