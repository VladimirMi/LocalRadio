package io.github.vladimirmi.localradio.data.repositories;

import android.util.Pair;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.ResourceManager;
import io.github.vladimirmi.localradio.data.models.StationRes;
import io.github.vladimirmi.localradio.data.models.StationsResult;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RxRetryTransformer;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 28.05.2018.
 */
public class SearchRepositoryImpl implements SearchRepository {

    private final RestService restService;
    private final CacheSource cacheSource;
    private final NetworkChecker networkChecker;
    private final Preferences preferences;
    private final ResourceManager resourceManager;
    private boolean skipCache;

    private final BehaviorRelay<SearchResult> searchResult = BehaviorRelay.create();

    @SuppressWarnings("WeakerAccess")
    @Inject
    public SearchRepositoryImpl(RestService restService,
                                CacheSource cacheSource,
                                NetworkChecker networkChecker,
                                Preferences preferences,
                                ResourceManager resourceManager) {
        this.restService = restService;
        this.cacheSource = cacheSource;
        this.networkChecker = networkChecker;
        this.preferences = preferences;
        this.resourceManager = resourceManager;

        if (!preferences.isSearchDone.get()) {
            searchResult.accept(SearchResult.notDone());
        } else {
            searchResult.accept(SearchResult.done());
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
    public Single<List<Station>> searchStationsManual(String countryCode, String city) {
        resolveCache(countryCode, city);

        return restService.getStationsByLocation(countryCode, city, 1)
                .doOnError(e -> cacheSource.cleanCache(countryCode, city))
                .compose(new RxRetryTransformer<>())
                .map(StationsResult::getStations)
                .map(this::mapResponse)
                .doOnSuccess(stations -> setSearchResult(SearchResult.doneManual(stations.size(),
                        resourceManager.getQuantityString(R.plurals.search_result_manual, stations.size()))))
                .doOnError(throwable -> setSearchResult(SearchResult.notDone()))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Station>> searchStationsByCoordinates(Pair<Float, Float> coordinates) {
        resolveCache(coordinates.first.toString(), coordinates.second.toString());

        return restService.getStationsByCoordinates(coordinates.first, coordinates.second)
                .doOnError(e -> cacheSource.cleanCache(coordinates.first.toString(), coordinates.second.toString()))
                .compose(new RxRetryTransformer<>())
                .map(StationsResult::getStations)
                .map(this::mapResponse)
                .doOnSuccess(stations -> setSearchResult(SearchResult.doneAuto(stations.size(),
                        resourceManager.getQuantityString(R.plurals.search_result_auto, stations.size()))))
                .doOnError(throwable -> setSearchResult(SearchResult.notDone()))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Station>> searchStationsByIp() {
        return Single.fromCallable(networkChecker::getIp)
                .flatMap(ip -> {
                    resolveCache(ip);
                    return restService.getStationsByIp(ip)
                            .doOnError(e -> cacheSource.cleanCache(ip))
                            .compose(new RxRetryTransformer<>());
                })
                .map(StationsResult::getStations)
                .map(this::mapResponse)
                .doOnSuccess(stations -> setSearchResult(SearchResult.doneAuto(stations.size(),
                        resourceManager.getQuantityString(R.plurals.search_result_manual, stations.size()))))
                .doOnError(throwable -> setSearchResult(SearchResult.notDone()))
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

}
