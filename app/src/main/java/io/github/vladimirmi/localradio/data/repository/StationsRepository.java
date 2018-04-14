package io.github.vladimirmi.localradio.data.repository;

import com.jakewharton.rxrelay2.BehaviorRelay;

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
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Completable;
import io.reactivex.Single;
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

    public final BehaviorRelay<List<Station>> stations = BehaviorRelay.createDefault(Collections.emptyList());
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

        searchStations().subscribeWith(new RxUtils.ErrorCompletableObserver(null));
    }

    public Completable searchStations() {
        Single<List<Station>> search;

        if (preferences.autodetect.get()) {
            search = searchStationsAuto();
        } else {
            search = searchStationsManual();
        }
        return search
                .onErrorReturn(throwable -> {
                    Timber.w(throwable);
                    return Collections.emptyList();
                })
                .flatMapCompletable(stations -> {
                    this.stations.accept(stations);
                    return updateCurrentStationFromPreferences(stations);
                });
    }

    public Completable refreshStations() {
        return Completable.fromAction(cacheSource::cleanCache)
                .andThen(searchStations());
    }

    public Completable setCurrentStation(Station station) {
        preferences.currentStation.put(station.getId());

//        if (station.isNullStation()) {
//            currentStation.accept(station);
//            return Completable.complete();

        if (station.getUrl() == null) {
            return restService.getStationUrl(station.getId())
                    .filter(stationUrlResult -> stationUrlResult.isSuccess() && !stationUrlResult.getResult().isEmpty())
                    .map(stationUrlResult -> stationUrlResult.getResult().get(0))
                    .doOnSuccess(this::updateStationsWith)
                    .ignoreElement();
        } else {
            currentStation.accept(station);
            return Completable.complete();
        }
    }

    public void updateStationsWith(Station... newStations) {
        boolean updated = false;
        List<Station> list = stations.getValue();
        int currentId = preferences.currentStation.get();

        for (Station station : list) {
            for (Station newStation : newStations) {
                if (station.getId() == newStation.getId()) {
                    updated = station.update(newStation);
                    if (station.getId() == currentId) {
                        currentStation.accept(station);
                    }
                    break;
                }
            }
        }
        if (updated) stations.accept(list);
    }

    private Single<List<Station>> searchStationsAuto() {
        return locationSource.getLastLocation()
                .flatMap(location -> restService
                        .getStationsByCoordinates(round(location.getLatitude()), round(location.getLongitude()))
                        .flatMap(stationsResult -> {
                            if (!stationsResult.isSuccess()) {
                                return restService.getStationsByIp(networkChecker.getIp());
                            } else {
                                return Single.just(stationsResult);
                            }
                        }))
                .map(StationsResult::getStations)
                .doOnSuccess(stations -> {
                    preferences.countryCode.put(stations.get(0).getCountryCode());
                    preferences.city.put("");
                });
    }

    private Single<List<Station>> searchStationsManual() {
        Single<List<Station>> result;
        String countryCode = preferences.countryCode.get();
        String city = preferences.city.get();
        if (countryCode.isEmpty() && city.isEmpty()) {
            result = Single.just(Collections.emptyList());
        } else {
            result = restService.getStationsByLocation(countryCode, city, 1)
                    .map(StationsResult::getStations);
        }
        return result;
    }

    private Completable updateCurrentStationFromPreferences(List<Station> stations) {
        Station newCurrentStation = null;
        for (Station station : stations) {
            if (station.getId() == preferences.currentStation.get()) {
                newCurrentStation = station;
            }
        }

        if (newCurrentStation == null || !stations.contains(newCurrentStation)) {
            if (stations.isEmpty()) {
                newCurrentStation = Station.nullStation();
            } else {
                newCurrentStation = stations.get(0);
            }
        }
        return setCurrentStation(newCurrentStation);
    }

    private double round(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
