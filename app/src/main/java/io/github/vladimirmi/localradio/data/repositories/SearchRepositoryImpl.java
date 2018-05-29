package io.github.vladimirmi.localradio.data.repositories;

import android.util.Pair;

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
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
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
    }

    @Override
    public boolean isSearchDone() {
        return preferences.isSearchDone.get();
    }

    @Override
    public void setSearchDone(boolean done) {
        preferences.isSearchDone.put(done);
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
