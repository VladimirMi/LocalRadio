package io.github.vladimirmi.localradio.domain;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class StationsInteractor {

    private final StationsRepository stationsRepository;
    private List<Station> filteredStations;
    private String filter = "";

    @Inject
    public StationsInteractor(StationsRepository stationsRepository) {
        this.stationsRepository = stationsRepository;
    }

    public Observable<List<Station>> getStationsObs() {
        return stationsRepository.getStationsObs()
                .map(this::filter);
    }

    public Observable<Station> getCurrentStationObs() {
        return stationsRepository.getCurrentStationObs()
                .distinctUntilChanged();
    }

    public Station getCurrentStation() {
        return stationsRepository.getCurrentStation();
    }

    public void setCurrentStation(Station station) {
        stationsRepository.setCurrentStation(station);
    }

    public void previousStation() {
        List<Station> source = getFilteredStations();
        int indexOfCurrent = source.indexOf(getCurrentStation());
        if (indexOfCurrent == -1) return;

        int indexOfPrevious = (indexOfCurrent + source.size() - 1) % source.size();
        setCurrentStation(source.get(indexOfPrevious));
    }

    public void nextStation() {
        List<Station> source = getFilteredStations();
        int indexOfCurrent = source.indexOf(getCurrentStation());
        if (indexOfCurrent == -1) return;

        int indexOfNext = (indexOfCurrent + 1) % source.size();
        setCurrentStation(source.get(indexOfNext));
    }

    // TODO: 4/30/18 return list to presentation
    public void filterStations(String filter) {
        this.filter = filter.toLowerCase();
        stationsRepository.setStations(stationsRepository.getStations());
    }

    public String getFilter() {
        return filter;
    }

    private List<Station> getFilteredStations() {
        if (filteredStations != null) {
            return filteredStations;
        }
        filteredStations = stationsRepository.getStations();
        return filteredStations;
    }

    private List<Station> filter(List<Station> stations) {
        if (filter.isEmpty()) {
            filteredStations = stations;
            return filteredStations;
        }
        filteredStations = new ArrayList<>();

        for (Station station : stations) {
            if (checkCanFilter(station.getName())
                    || checkCanFilter(station.getGenre())
                    || checkCanFilter(station.getDial())
                    || checkCanFilter(station.getBand())) {
                filteredStations.add(station);
            }
        }
        return filteredStations;
    }

    private boolean checkCanFilter(String field) {
        return field != null && field.toLowerCase().contains(filter);
    }
}
