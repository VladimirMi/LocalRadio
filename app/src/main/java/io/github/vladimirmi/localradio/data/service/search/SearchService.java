package io.github.vladimirmi.localradio.data.service.search;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.entity.StationsResult;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.FavoriteInteractor;
import io.github.vladimirmi.localradio.utils.UiUtils;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import toothpick.Toothpick;

/**
 * Created by Vladimir Mikhalev 30.04.2018.
 */
public class SearchService extends IntentService {

    private static final String EXTRA_SKIP_CACHE = "EXTRA_SKIP_CACHE";

    @Inject RestService restService;
    @Inject CacheSource cacheSource;
    @Inject StationsRepository stationsRepository;
    @Inject LocationRepository locationRepository;
    @Inject FavoriteInteractor favoriteInteractor;
    @Inject NetworkChecker networkChecker;

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
    protected void onHandleIntent(Intent intent) {
        boolean skipCache = intent.getBooleanExtra(EXTRA_SKIP_CACHE, false);
        stationsRepository.setSearching(true);
        Throwable throwable = search(skipCache).blockingGet();
        // TODO: 5/9/18 MessageException from throwable
        // TODO: 5/9/18 Retry policy
        if (throwable != null) {
            stationsRepository.setSearchDone(false);
            UiUtils.handleError(this, throwable);
        }
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
        }).toCompletable();
    }

    private Single<List<Station>> searchStationsAuto(boolean skipCache) {
        return locationRepository.getCoordinates()
                .subscribeOn(Schedulers.io())
                .flatMap(coordinates -> {
                    Pair<String, String> countryCodeCity = locationRepository.getCountryCodeCity(coordinates);

                    if (countryCodeCity == null) {
                        return getStationsByIp(skipCache)
                                .doOnSuccess(locationRepository::saveCountryCodeCity);
                    }
                    locationRepository.saveCountryCodeCity(countryCodeCity);

                    if (countryCodeCity.first.equals("US")) {
                        return getStationsByCoordinates(skipCache, coordinates);
                    } else {
                        return searchStationsManual(skipCache);
                    }
                })
                .onErrorReturn(throwable -> Collections.emptyList());
    }

    private Single<List<Station>> searchStationsManual(boolean skipCache) {
        String countryCode = locationRepository.getCountryCode();
        String city = locationRepository.getCity();
        if (skipCache) {
            cacheSource.cleanCache(String.format("%s_%s", countryCode, city));
        }
        return restService.getStationsByLocation(countryCode, city, 1)
                .map(StationsResult::getStations)
                .onErrorReturn(throwable -> Collections.emptyList())
                .subscribeOn(Schedulers.io());
    }

    private Single<List<Station>> getStationsByCoordinates(boolean skipCache, Pair<Float, Float> coordinates) {
        if (skipCache) {
            cacheSource.cleanCache(String.format("%s_%s", coordinates.first, coordinates.second));
        }
        return restService.getStationsByCoordinates(coordinates.first, coordinates.second)
                .map(StationsResult::getStations)
                .subscribeOn(Schedulers.io());
    }

    private Single<List<Station>> getStationsByIp(boolean skipCache) {
        return Single.fromCallable(() -> networkChecker.getIp())
                .flatMap(ip -> {
                    if (skipCache) cacheSource.cleanCache(ip);
                    return restService.getStationsByIp(ip);
                })
                .map(StationsResult::getStations)
                .subscribeOn(Schedulers.io());
    }
}
