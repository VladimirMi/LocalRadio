package io.github.vladimirmi.localradio.domain.repositories;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.Station;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 30.05.2018.
 */
public interface StationsRepository {

    void resetStations();

    void setSearchResult(List<Station> stations);

    void setStations(List<Station> stations);

    List<Station> getStations();

    Observable<List<Station>> getStationsObs();

    void setCurrentStation(Station station);

    Station getCurrentStation();

    Observable<Station> getCurrentStationObs();
}
