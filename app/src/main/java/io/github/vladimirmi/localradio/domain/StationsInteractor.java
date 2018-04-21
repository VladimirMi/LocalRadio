package io.github.vladimirmi.localradio.domain;

import java.util.ArrayList;
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
    private List<Station> stations;
    private String filter = "";

    @Inject
    public StationsInteractor(StationsRepository stationsRepository) {
        this.stationsRepository = stationsRepository;
    }

    public Observable<List<Station>> getStationsObs() {
        return stationsRepository.stations
                .map(this::filter)
                .doOnNext(it -> stations = it);
    }

    public Observable<Station> getCurrentStationObs() {
        return stationsRepository.currentStation;
    }

    public Observable<Station> getCurrentStationWithUrlObs() {
        return stationsRepository.currentStation.filter(station -> station.getUrl() != null);
    }

    public Station getCurrentStation() {
        return stationsRepository.currentStation.getValue();
    }

    public Completable setCurrentStation(Station station) {
        return stationsRepository.setCurrentStation(station);
    }

    public Completable previousStation() {
        int indexOfCurrent = stations.indexOf(getCurrentStation());
        if (indexOfCurrent == -1) return Completable.complete();

        int indexOfPrevious = (indexOfCurrent + stations.size() - 1) % stations.size();

        return setCurrentStation(stations.get(indexOfPrevious));
    }

    public Completable nextStation() {
        int indexOfCurrent = stations.indexOf(getCurrentStation());
        if (indexOfCurrent == -1) return Completable.complete();

        int indexOfNext = (indexOfCurrent + 1) % stations.size();

        return setCurrentStation(stations.get(indexOfNext));
    }

    public void filterStations(String filter) {
        this.filter = filter.toLowerCase();
        stationsRepository.stations.accept(stationsRepository.stations.getValue());
    }

    private List<Station> filter(List<Station> stations) {
        if (filter.isEmpty()) return stations;

        List<Station> filtered = new ArrayList<>();

        for (Station station : stations) {
            if (checkCanFilter(station.getCallsign())
                    || checkCanFilter(station.getGenre())
                    || checkCanFilter(station.getDial())
                    || checkCanFilter(station.getBand())) {
                filtered.add(station);
            }
        }
        return filtered;
    }

    private boolean checkCanFilter(String field) {
        return field != null && field.toLowerCase().contains(filter);
    }

    public String getFilter() {
        return filter;
    }
}
