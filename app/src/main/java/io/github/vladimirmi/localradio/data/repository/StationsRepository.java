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
import io.reactivex.Single;
import io.reactivex.SingleSource;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsRepository {

    private final RestService restService;
    private final LocationSource locationSource;
    private final Preferences preferences;
    private final NetworkChecker networkChecker;
    private final CacheSource cacheSource;

    // TODO: 4/28/18 make private
    public final BehaviorRelay<List<Station>> stations = BehaviorRelay.create();
    public final BehaviorRelay<Station> currentStation = BehaviorRelay.createDefault(Station.nullStation());

    @Inject
    public StationsRepository(RestService restService,
                              LocationSource locationSource,
                              Preferences preferences,
                              NetworkChecker networkChecker,
                              CacheSource cacheSource) {
        this.restService = restService;
        this.locationSource = locationSource;
        this.preferences = preferences;
        this.networkChecker = networkChecker;
        this.cacheSource = cacheSource;
    }

    public boolean isSearchDone() {
        return preferences.isSearchDone.get();
    }

    public void setSearchDone(boolean done) {
        preferences.isSearchDone.put(done);
    }

    public void setCurrentStation(Station station) {
        preferences.currentStation.put(station.getId());
        currentStation.accept(station);
    }

    public Single<List<Station>> searchStationsAuto(boolean skipCache) {
        return getStationsByCoordinates(skipCache)
                .flatMap(stationsResult -> {
                    if (!stationsResult.isSuccess()) {
                        return getStationsByIp(skipCache);
                    } else {
                        return Single.just(stationsResult);
                    }
                })
                .map(StationsResult::getStations);
    }

    public Single<List<Station>> searchStationsManual(boolean skipCache) {
        String countryCode = preferences.countryCode.get();
        String city = preferences.city.get();
        if (skipCache) cacheSource.cleanCache(String.format("%s_%s", countryCode, city));
        return restService.getStationsByLocation(countryCode, city, 1)
                .map(StationsResult::getStations)
                .onErrorReturn(throwable -> Collections.emptyList());
    }

    public void resetStations() {
        stations.accept(Collections.emptyList());
        if (!currentStation.getValue().isFavorite()) {
            currentStation.accept(Station.nullStation());
        }
    }

    // TODO: 4/26/18 move logic to search interactor
    public void updateCurrentStationFromPreferences(List<Station> stations) {
        Station newCurrentStation = Station.nullStation();
        Integer currentId = preferences.currentStation.get();
        for (Station station : stations) {
            if (station.getId() == currentId) {
                newCurrentStation = station;
                break;
            }
        }
        if (newCurrentStation.isNullStation() && !stations.isEmpty()) {
            newCurrentStation = stations.get(0);
        }
        setCurrentStation(newCurrentStation);
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
}
