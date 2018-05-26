package io.github.vladimirmi.localradio.data.repository;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsRepository {

    private final Preferences preferences;

    private final BehaviorRelay<List<Station>> stations = BehaviorRelay.create();
    private final BehaviorRelay<Station> currentStation = BehaviorRelay.create();
    private BehaviorRelay<Boolean> isSearching = BehaviorRelay.createDefault(false);

    @Inject
    public StationsRepository(Preferences preferences) {
        this.preferences = preferences;
    }

    public boolean isSearchDone() {
        return preferences.isSearchDone.get();
    }

    public void setSearchDone(boolean done) {
        preferences.isSearchDone.put(done);
        setSearching(false);
    }

    public void resetSearch() {
        setSearchDone(false);
        stations.accept(Collections.emptyList());
        if (currentStation.hasValue()) {
            setCurrentStation(Station.nullObject());
        }
    }

    public void setSearchResult(List<Station> stations) {
        setSearchDone(true);
        updateCurrentStationFromPreferences(stations);
        this.stations.accept(stations);
    }

    public void setSearching(boolean isSearching) {
        this.isSearching.accept(isSearching);
    }

    public Observable<Boolean> isSearching() {
        return isSearching;
    }

    public void setStations(List<Station> stations) {
        this.stations.accept(stations);
    }

    public List<Station> getStations() {
        return stations.hasValue() ? stations.getValue() : Collections.emptyList();
    }

    public Observable<List<Station>> getStationsObs() {
        return stations;
    }

    public void setCurrentStation(Station station) {
//        preferences.currentStationIsFavorite.put(station.isFavorite());
        preferences.currentStationId.put(station.id);
        currentStation.accept(station);
    }

    public Station getCurrentStation() {
        return currentStation.getValue();
    }

    public Observable<Station> getCurrentStationObs() {
        return currentStation;
    }

    private void updateCurrentStationFromPreferences(List<Station> stations) {
        if (preferences.currentStationIsFavorite.get()) return;

        Station newCurrentStation = Station.nullObject();
        Integer currentId = preferences.currentStationId.get();
        for (Station station : stations) {
            if (station.id == currentId) {
                newCurrentStation = station;
                break;
            }
        }
        if (newCurrentStation.isNullObject && !stations.isEmpty()) {
            newCurrentStation = stations.get(0);
        }
        setCurrentStation(newCurrentStation);
    }
}
