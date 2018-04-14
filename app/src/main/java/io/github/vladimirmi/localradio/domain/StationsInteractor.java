package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class StationsInteractor {

    private final StationsRepository stationsRepository;

    @Inject
    public StationsInteractor(StationsRepository stationsRepository) {
        this.stationsRepository = stationsRepository;
    }

    public Observable<List<Station>> getStationsObs() {
        return stationsRepository.stations;
    }

    public Observable<Station> getCurrentStationObs() {
        return stationsRepository.currentStation;
    }

    public Station getCurrentStation() {
        return stationsRepository.currentStation.getValue();
    }

    public Completable setCurrentStation(Station station) {
        return stationsRepository.setCurrentStation(station);
    }

    public Completable previousStation() {
        return Completable.complete();
    }

    public Completable nextStation() {
        return Completable.complete();
    }
}
