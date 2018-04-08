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
import io.github.vladimirmi.localradio.data.source.LocationSource;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsRepository {

    private final RestService restService;
    private final LocationSource locationSource;
    private final Preferences preferences;
    private final NetworkChecker networkChecker;

    private final BehaviorRelay<List<Station>> stations = BehaviorRelay.create();
    private final BehaviorRelay<Station> currentStation = BehaviorRelay.create();

    @Inject
    public StationsRepository(RestService restService,
                              LocationSource locationSource,
                              Preferences preferences,
                              NetworkChecker networkChecker) {
        this.restService = restService;
        this.locationSource = locationSource;
        this.preferences = preferences;
        this.networkChecker = networkChecker;
    }

    public Observable<List<Station>> getStations() {
        if (stations.hasValue()) {
            return stations;
        } else {
            return refreshStations().andThen(stations);
        }
    }

    public Completable refreshStations() {
        Single<List<Station>> result;

        if (preferences.autodetect.get()) {
            result = locationSource.getLastLocation()
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

        } else {
            String countryCode = preferences.countryCode.get();
            if (countryCode.isEmpty()) {
                result = Single.just(Collections.emptyList());
            } else {
                result = restService.getStationsByLocation(countryCode, preferences.city.get())
                        .map(StationsResult::getStations);
            }
        }
        return result.doOnSuccess(this.stations)
                .toCompletable();
    }

    public BehaviorRelay<Station> getCurrentStation() {
        return currentStation;
    }

    public Completable setCurrentStation(Station station) {
        Completable ensureHaveUrl;

        if (station.getUrl() == null) {
            ensureHaveUrl = restService.getStationUrl(station.getId())
                    .filter(stationUrlResult -> stationUrlResult.isSuccess() && !stationUrlResult.getResult().isEmpty())
                    .map(stationUrlResult -> stationUrlResult.getResult().get(0))
                    .doOnSuccess(stationWithUrl -> {
                        List<Station> newList = stations.getValue();
                        for (Station newStation : newList) {
                            if (newStation.getId() == stationWithUrl.getId()) {
                                newStation.setUrl(stationWithUrl.getUrl());
                                currentStation.accept(newStation);
                                stations.accept(newList);
                                break;
                            }
                        }
                    }).ignoreElement();
        } else {
            ensureHaveUrl = Completable.fromAction(() -> currentStation.accept(station));
        }

        return ensureHaveUrl;
    }

    private double round(double x) {
        return Math.round(x * 100.0) / 100.0;
    }
}
