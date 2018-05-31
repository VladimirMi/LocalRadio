package io.github.vladimirmi.localradio.data.repositories;

import android.util.Pair;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.models.StationRes;
import io.github.vladimirmi.localradio.data.models.StationsResult;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RxRetryTransformer;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.domain.models.SearchState;
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
    private boolean skipCache;

    private final BehaviorRelay<SearchState> searchState = BehaviorRelay.create();

    @SuppressWarnings("WeakerAccess")
    @Inject
    public SearchRepositoryImpl(RestService restService,
                                CacheSource cacheSource,
                                NetworkChecker networkChecker,
                                Preferences preferences) {
        this.restService = restService;
        this.cacheSource = cacheSource;
        this.networkChecker = networkChecker;
        this.preferences = preferences;

        if (!preferences.isSearchDone.get()) {
            searchState.accept(SearchState.NOT_DONE);
        } else if (preferences.autodetect.get()) {
            searchState.accept(SearchState.AUTO_DONE);
        } else {
            searchState.accept(SearchState.MANUAL_DONE);
        }
    }

    @Override
    public Observable<SearchState> searchState() {
        return searchState;
    }

    @Override
    public void setSearchState(SearchState state) {
        searchState.accept(state);
        preferences.isSearchDone.put(state.isSearchDone());
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
                .doOnSuccess(stations -> setSearchState(SearchState.MANUAL_DONE))
                .doOnError(throwable -> setSearchState(SearchState.NOT_DONE))
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
                .doOnSuccess(stations -> setSearchState(SearchState.AUTO_DONE))
                .doOnError(throwable -> setSearchState(SearchState.NOT_DONE))
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
                .doOnSuccess(stations -> setSearchState(SearchState.AUTO_DONE))
                .doOnError(throwable -> setSearchState(SearchState.NOT_DONE))
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
