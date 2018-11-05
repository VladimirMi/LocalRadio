package io.github.vladimirmi.localradio.data.repositories;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.models.StationRes;
import io.github.vladimirmi.localradio.data.models.StationsResult;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RxRetryTransformer;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
import io.github.vladimirmi.localradio.map.MapPosition;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 28.05.2018.
 */
public class SearchRepositoryImpl implements SearchRepository {

    private final RestService restService;
    private final CacheSource cacheSource;
    private final Preferences preferences;
    private boolean skipCache;

    private final BehaviorRelay<SearchResult> searchResult = BehaviorRelay.create();

    @SuppressWarnings("WeakerAccess")
    @Inject
    public SearchRepositoryImpl(RestService restService,
                                CacheSource cacheSource,
                                Preferences preferences) {
        this.restService = restService;
        this.cacheSource = cacheSource;
        this.preferences = preferences;


        if (!preferences.isSearchDone.get()) {
            searchResult.accept(SearchResult.notDone());
        } else {
            searchResult.accept(SearchResult.done(0));
        }
    }

    @Override
    public Observable<SearchResult> searchResult() {
        return searchResult;
    }

    @Override
    public void setSearchResult(SearchResult result) {
        searchResult.accept(result);
        preferences.isSearchDone.put(result.isSearchDone());
    }

    @Override
    public void setSkipCache(boolean skipCache) {
        this.skipCache = skipCache;
    }

    @Override
    public Single<List<Station>> searchStationsByCoordinates(MapPosition position) {
        resolveCache(String.valueOf(position.latitude), String.valueOf(position.longitude));
        Timber.e("searchStationsByCoordinates: %s", position.getLatLng().toString());
        return restService.getStationsByCoordinates(position.latitude, position.longitude)
                .doOnError(e -> cacheSource.cleanCache(String.valueOf(position.latitude),
                        String.valueOf(position.longitude)))
                .compose(new RxRetryTransformer<>())
                .map(StationsResult::getStations)
                .map(this::mapResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Station>> searchStationsByCountry(String country) {
        Timber.w("searchStationsByCountry: %s", country);
        resolveCache(country);
        return restService.getStationsByLocation(country, 1)
                .doOnError(e -> cacheSource.cleanCache(country))
                .compose(new RxRetryTransformer<>())
                .map(StationsResult::getStations)
                .map(this::mapResponse)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Station>> searchStationsByCity(String country, String city) {
        Timber.w("searchStationsByCity: %s, %s", country, city);
        resolveCache(country, city);
        return restService.getStationsByLocation(country, city, 1)
                .doOnError(e -> cacheSource.cleanCache(country, city))
                .compose(new RxRetryTransformer<>())
                .map(StationsResult::getStations)
                .map(this::mapResponse)
                .subscribeOn(Schedulers.io());
    }

    private void resolveCache(String... queries) {
        if (skipCache) {
            cacheSource.cleanCache(queries);
            skipCache = false;
        }
    }

    private List<Station> mapResponse(List<StationRes> stationRes) {
        List<Station> stations = new ArrayList<>(stationRes.size());
        for (StationRes res : stationRes) {
            stations.add(new Station(res));
        }
        return stations;
    }

    @Override
    public void saveSearchMode(int mode) {
        preferences.searchMode.put(mode);
    }

    @Override
    public int getSearchMode() {
        return preferences.searchMode.get();
    }
}
