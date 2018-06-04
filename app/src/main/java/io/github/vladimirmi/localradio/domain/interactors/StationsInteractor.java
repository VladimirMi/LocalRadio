package io.github.vladimirmi.localradio.domain.interactors;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.StationsRepository;
import io.reactivex.Observable;
import timber.log.Timber;

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
        return stationsRepository.getStationsObs();
    }

    public Observable<List<Station>> getFilteredStationsObs() {
        return stationsRepository.getStationsObs()
                .map(this::filter);
    }

    public List<Station> getStations() {
        return stationsRepository.getStations();
    }

    public Observable<Station> getCurrentStationObs() {
        return stationsRepository.getCurrentStationObs();
    }

    public Station getCurrentStation() {
        Station currentStation = stationsRepository.getCurrentStation();
        return currentStation == null ? Station.nullObject() : currentStation;
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
        Timber.e("nextStation: " + source.size());
        int indexOfCurrent = source.indexOf(getCurrentStation());
        if (indexOfCurrent == -1) return;
        Timber.e("nextStation: " + indexOfCurrent);

        int indexOfNext = (indexOfCurrent + 1) % source.size();
        setCurrentStation(source.get(indexOfNext));
    }

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

    private boolean checkCanFilter(String field) {
        return field != null && field.toLowerCase().contains(filter);
    }

    private List<Station> filter(List<Station> stations) {
        if (filter.isEmpty()) {
            filteredStations = stations;
            return filteredStations;
        }
        filteredStations = new ArrayList<>();

        for (Station station : stations) {
            if (checkCanFilter(station.name)
                    || checkCanFilter(station.genre)
                    || checkCanFilter(station.dial)
                    || checkCanFilter(station.band)) {
                filteredStations.add(station);
            }
        }
        return filteredStations;
    }
}
