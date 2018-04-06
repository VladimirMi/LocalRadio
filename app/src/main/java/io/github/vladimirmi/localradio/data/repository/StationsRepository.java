package io.github.vladimirmi.localradio.data.repository;

import com.jakewharton.rxrelay2.BehaviorRelay;

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
                            .getStationsByCoordinates(location.getLatitude(), location.getLongitude())
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
            result = restService.getStationsByLocation(preferences.countryCode.get(), preferences.city.get())
                    .map(StationsResult::getStations);
        }
        return result.doOnSuccess(this.stations)
                .toCompletable();
    }
}
