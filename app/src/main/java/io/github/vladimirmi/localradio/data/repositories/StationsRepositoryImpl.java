package io.github.vladimirmi.localradio.data.repositories;

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

public class StationsRepositoryImpl {

    private final Preferences preferences;

    private final BehaviorRelay<List<Station>> stations = BehaviorRelay.create();
    private final BehaviorRelay<Station> currentStation = BehaviorRelay.create();

    @Inject
    public StationsRepositoryImpl(Preferences preferences) {
        this.preferences = preferences;
    }

    public void resetStations() {
        stations.accept(Collections.emptyList());
        if (currentStation.hasValue()) {
            setCurrentStation(Station.nullObject());
        }
    }

    public void setSearchResult(List<Station> stations) {
        updateCurrentStationFromPreferences(stations);
        this.stations.accept(stations);
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
