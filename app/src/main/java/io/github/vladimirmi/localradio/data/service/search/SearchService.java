package io.github.vladimirmi.localradio.data.service.search;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.entity.StationsResult;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.FavoriteInteractor;
import io.reactivex.Completable;
import io.reactivex.Single;
import timber.log.Timber;
import toothpick.Toothpick;

/**
 * Created by Vladimir Mikhalev 30.04.2018.
 */
public class SearchService extends IntentService {

    private static final String EXTRA_SKIP_CACHE = "EXTRA_SKIP_CACHE";

    @Inject RestService restService;
    @Inject CacheSource cacheSource;
    @Inject LocationRepository locationRepository;
    @Inject StationsRepository stationsRepository;
    @Inject FavoriteInteractor favoriteInteractor;

    public SearchService() {
        super("SearchService");
    }

    public static void performSearch(boolean skipCache) {
        Context context = Scopes.appContext();
        Intent startSearchService = new Intent(context, SearchService.class);
        startSearchService.putExtra(EXTRA_SKIP_CACHE, skipCache);
        context.startService(startSearchService);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toothpick.inject(this, Scopes.getAppScope());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO: 4/30/18 get current address

        boolean skipCache = intent.getBooleanExtra(EXTRA_SKIP_CACHE, false);
        Throwable throwable = search(skipCache).blockingGet();
        Timber.w(throwable);
    }

    private Completable search(boolean skipCache) {
        Single<List<Station>> search;

        if (locationRepository.isAutodetect()) {
            search = searchStationsAuto(skipCache);
        } else {
            search = searchStationsManual(skipCache);
        }

        return search.doOnSuccess(stations -> {
            stationsRepository.setSearchResult(stations);
            favoriteInteractor.updateStationsWithFavorites();
        })
                .doOnError(throwable -> stationsRepository.setSearchDone(false))
                .toCompletable();
    }

    private Single<List<Station>> searchStationsAuto(boolean skipCache) {
        return getStationsByCoordinates(skipCache)
                .doOnSuccess(stations -> {
                    // TODO: 4/24/18 save more specific location
                    locationRepository.saveCountryCodeCity(stations.get(0).getCountryCode(), "");
                });
    }

    private Single<List<Station>> searchStationsManual(boolean skipCache) {
        String countryCode = locationRepository.getCountryCode();
        String city = locationRepository.getCity();
        if (skipCache) cacheSource.cleanCache(String.format("%s_%s", countryCode, city));
        return restService.getStationsByLocation(countryCode, city, 1)
                .map(StationsResult::getStations)
                .onErrorReturn(throwable -> Collections.emptyList());
    }

    private Single<List<Station>> getStationsByCoordinates(boolean skipCache) {
        return locationRepository.getCoordinates()
                .flatMap(coordinates -> {
                    if (skipCache) {
                        cacheSource.cleanCache(String.format("%s_%s", coordinates.first, coordinates.second));
                    }
                    return restService.getStationsByCoordinates(coordinates.first, coordinates.second);
                }).map(StationsResult::getStations);
    }

//    private Single<StationsResult> getStationsByIp(boolean skipCache) throws IOException {
//        String ip = networkChecker.getIp();
//        if (skipCache) cacheSource.cleanCache(ip);
//        return restService.getStationsByIp(ip);
//    }
}
