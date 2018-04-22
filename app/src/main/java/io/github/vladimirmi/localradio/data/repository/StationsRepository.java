package io.github.vladimirmi.localradio.data.repository;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.entity.StationsResult;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.data.source.LocationSource;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsRepository {

    private final RestService restService;
    private final LocationSource locationSource;
    private final Preferences preferences;
    private final NetworkChecker networkChecker;
    private final CacheSource cacheSource;
    private final FavoriteRepository favoriteRepository;

    public final BehaviorRelay<List<Station>> stations = BehaviorRelay.createDefault(Collections.emptyList());
    public final BehaviorRelay<Station> currentStation = BehaviorRelay.createDefault(Station.nullStation());

    @Inject
    public StationsRepository(RestService restService,
                              LocationSource locationSource,
                              Preferences preferences,
                              NetworkChecker networkChecker,
                              CacheSource cacheSource, FavoriteRepository favoriteRepository) {
        this.restService = restService;
        this.locationSource = locationSource;
        this.preferences = preferences;
        this.networkChecker = networkChecker;
        this.cacheSource = cacheSource;
        this.favoriteRepository = favoriteRepository;
    }

    public Completable searchStations() {
        return searchStations(false);
    }

    public Completable refreshStations() {
        return Completable.fromAction(() -> stations.accept(Collections.emptyList()))
                .andThen(searchStations(true));
    }

    private Completable searchStations(boolean skipCache) {
        Single<List<Station>> search;

        if (preferences.autodetect.get()) {
            search = searchStationsAuto(skipCache);
        } else {
            search = searchStationsManual(skipCache);
        }
        return search
                .onErrorReturn(throwable -> {
                    Timber.w(throwable);
                    return Collections.emptyList();
                })
                .flatMapCompletable(stations -> {
                    updateStationsIfFavorite(stations);
                    this.stations.accept(stations);
                    return updateCurrentStationFromPreferences(stations);
                });
    }

    public Completable setCurrentStation(Station station) {
        preferences.currentStation.put(station.getId());
        currentStation.accept(station);

        if (!station.isNullStation() && station.getUrl() == null) {
            return restService.getStationUrl(station.getId())
                    .filter(stationUrlResult -> stationUrlResult.isSuccess() && !stationUrlResult.getResult().isEmpty())
                    .map(stationUrlResult -> stationUrlResult.getResult().get(0))
                    .doOnSuccess(stationWithUrl -> {
                        Station copy = station.setUrl(stationWithUrl.getUrl());
                        updateStationsWith(copy);
                        currentStation.accept(copy);
                    })
                    .ignoreElement();
        } else {
            return Completable.complete();
        }
    }

    public boolean updateStationsIfFavorite(List<Station> stations) {
        boolean updated = false;
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            boolean isFavorite = false;
            for (Station favoriteStation : favoriteRepository.getFavoriteStations()) {
                if (station.getId() == favoriteStation.getId()) {
                    isFavorite = true;
                    break;
                }
            }
            if (station.isFavorite() != isFavorite) {
                stations.set(i, station.setFavorite(isFavorite));
                updated = true;
            }
        }
        return updated;
    }

    private Single<List<Station>> searchStationsAuto(boolean skipCache) {
        return getStationsByCoordinates(skipCache)
                .flatMap(stationsResult -> {
                    if (!stationsResult.isSuccess()) {
                        return getStationsByIp(skipCache);
                    } else {
                        return Single.just(stationsResult);
                    }
                })
                .map(StationsResult::getStations)
                .doOnSuccess(stations -> {
                    preferences.countryCode.put(stations.get(0).getCountryCode());
                    preferences.city.put("");
                });
    }

    private Single<StationsResult> getStationsByCoordinates(boolean skipCache) {
        return locationSource.getLastLocation()
                .flatMap(location -> {
                    double latitude = Math.round(location.getLatitude() * 100.0) / 100.0;
                    double longitude = Math.round(location.getLongitude() * 100.0) / 100.0;

                    if (skipCache) {
                        cacheSource.cleanCache(String.format("%s_%s", latitude, longitude));
                    }
                    return restService.getStationsByCoordinates(latitude, longitude);
                });
    }

    private SingleSource<? extends StationsResult> getStationsByIp(boolean skipCache) throws IOException {
        String ip = networkChecker.getIp();
        if (skipCache) cacheSource.cleanCache(ip);
        return restService.getStationsByIp(ip);
    }

    private Single<List<Station>> searchStationsManual(boolean skipCache) {
        Single<List<Station>> result;
        String countryCode = preferences.countryCode.get();
        String city = preferences.city.get();
        if (countryCode.isEmpty() && city.isEmpty()) {
            result = Single.just(Collections.emptyList());
        } else {
            if (skipCache) cacheSource.cleanCache(String.format("%s_%s", countryCode, city));
            result = restService.getStationsByLocation(countryCode, city, 1)
                    .map(StationsResult::getStations);
        }
        return result;
    }

    private Completable updateCurrentStationFromPreferences(List<Station> stations) {
        Station newCurrentStation = Station.nullStation();
        Integer currentId = preferences.currentStation.get();
        for (Station station : stations) {
            if (station.getId() == currentId) {
                newCurrentStation = station;
                break;
            }
        }
        for (Station station : favoriteRepository.getFavoriteStations()) {
            if (station.getId() == currentId) {
                newCurrentStation = station;
                break;
            }
        }
        if (newCurrentStation.isNullStation() && !stations.isEmpty()) {
            newCurrentStation = stations.get(0);
        }
        return setCurrentStation(newCurrentStation);
    }

    private void updateStationsWith(Station station) {
        List<Station> stationList = stations.getValue();
        int index = stationList.indexOf(station);
        stationList.set(index, station);
        stations.accept(stationList);
    }

}
