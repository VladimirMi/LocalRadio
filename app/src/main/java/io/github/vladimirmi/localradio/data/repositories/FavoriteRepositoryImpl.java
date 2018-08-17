package io.github.vladimirmi.localradio.data.repositories;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.favorite.AppDatabase;
import io.github.vladimirmi.localradio.data.db.favorite.StationEntity;
import io.github.vladimirmi.localradio.data.db.favorite.StationsDao;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.FavoriteRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final StationsDao dao;
    private final Preferences preferences;

    private BehaviorRelay<List<Station>> favoriteStations = BehaviorRelay.create();

    @SuppressWarnings("WeakerAccess")
    @Inject
    public FavoriteRepositoryImpl(AppDatabase database,
                                  Preferences preferences) {
        this.dao = database.stationsDao();
        this.preferences = preferences;

        initFavorites().subscribe();
    }

    @Override
    public Observable<List<Station>> getFavoriteStationsObs() {
        return favoriteStations;
    }

    @Override
    public List<Station> getFavoriteStations() {
        return favoriteStations.getValue();
    }

    @Override
    public Completable addFavorite(Station station) {
        return Completable.fromAction(() -> dao.insertStation(new StationEntity(station)));
    }

    @Override
    public Completable removeFavorite(Station station) {
        return Completable.fromAction(() -> dao.deleteStation(station.id));
    }

    @Override
    public void setCurrentStationIsFavorite(boolean isFavorite) {
        preferences.currentStationIsFavorite.put(isFavorite);
    }

    @Override
    public int getCurrentFavoriteStationId() {
        return preferences.currentStationIsFavorite.get() ? preferences.currentStationId.get() : -1;
    }

    private Flowable<List<Station>> initFavorites() {
        return dao.getStations()
                .map(stationEntities -> {
                    List<Station> stations = new ArrayList<>(stationEntities.size());
                    for (StationEntity stationEntity : stationEntities) {
                        stations.add(new Station(stationEntity));
                    }
                    return stations;
                })
                .doOnNext(stations1 -> favoriteStations.accept(stations1))
                .subscribeOn(Schedulers.io());
    }

}
