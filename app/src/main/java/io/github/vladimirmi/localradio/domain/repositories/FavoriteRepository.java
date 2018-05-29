package io.github.vladimirmi.localradio.domain.repositories;

import java.util.List;

import io.github.vladimirmi.localradio.domain.models.Station;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 27.05.2018.
 */
public interface FavoriteRepository {

    Observable<List<Station>> getFavoriteStationsObs();

    List<Station> getFavoriteStations();

    Completable addFavorite(Station station);

    Completable removeFavorite(Station station);

    void setCurrentStationIsFavorite(boolean isFavorite);

    int getCurrentFavoriteStationId();
}
