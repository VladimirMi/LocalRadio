package io.github.vladimirmi.localradio.data.repositories;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.StationsRepository;
import io.reactivex.Observable;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsRepositoryImpl implements StationsRepository {

    private final Preferences preferences;

    private final BehaviorRelay<List<Station>> stations = BehaviorRelay.createDefault(Collections.emptyList());
    private final BehaviorRelay<Station> currentStation = BehaviorRelay.create();

    @Inject
    public StationsRepositoryImpl(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void resetStations() {
        stations.accept(Collections.emptyList());
        if (currentStation.hasValue() && !preferences.currentStationIsFavorite.get()) {
            setCurrentStation(Station.nullObject());
        }
    }

    @Override
    public void setSearchResult(List<Station> stations) {
        updateCurrentStationFromPreferences(stations);
        this.stations.accept(stations);
    }

    @Override
    public void setStations(List<Station> stations) {
        this.stations.accept(stations);
    }

    @Override
    public List<Station> getStations() {
        Timber.e("getStations: " + stations.getValue());
        return stations.hasValue() ? stations.getValue() : Collections.emptyList();
    }

    @Override
    public Observable<List<Station>> getStationsObs() {
        return stations;
    }

    @Override
    public void setCurrentStation(Station station) {
        preferences.currentStationId.put(station.id);
        currentStation.accept(station);
    }

    @Override
    public Station getCurrentStation() {
        return currentStation.getValue();
    }

    @Override
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
        Timber.e("updateCurrentStationFromPreferences: " + newCurrentStation);
        if (newCurrentStation.isNullObject && !stations.isEmpty()) {
            newCurrentStation = stations.get(0);
        }
        setCurrentStation(newCurrentStation);
    }
}
