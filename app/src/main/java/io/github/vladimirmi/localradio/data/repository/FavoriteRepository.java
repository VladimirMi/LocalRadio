package io.github.vladimirmi.localradio.data.repository;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.AppDatabase;
import io.github.vladimirmi.localradio.data.db.StationEntity;
import io.github.vladimirmi.localradio.data.db.StationsDao;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteRepository {

    private final StationsDao dao;
    private final Preferences preferences;

    private BehaviorRelay<List<Station>> favoriteStations = BehaviorRelay.create();

    @SuppressWarnings("WeakerAccess")
    @Inject
    public FavoriteRepository(AppDatabase database,
                              Preferences preferences) {
        this.dao = database.stationsDao();
        this.preferences = preferences;

        initFavorites().subscribe();
    }

    public Observable<List<Station>> getFavoriteStationsObs() {
        return favoriteStations;
    }

    public List<Station> getFavoriteStations() {
        return favoriteStations.getValue();
    }

    public Completable addFavorite(Station station) {
        return Completable.fromAction(() -> dao.insertStation(new StationEntity(station)));
    }

    public Completable removeFavorite(Station station) {
        return Completable.fromAction(() -> dao.deleteStation(station.id));
    }

    public Station findCurrentFavoriteStation() {
//        int currentId = preferences.currentStationId.get();
//        // return first if current station is null object
//        if (currentId == 0 && !favoriteStations.isEmpty()) return favoriteStations.get(0);
//        for (Station station : favoriteStations) {
//            if (station.id == currentId) {
//                return station;
//            }
//        }
        return null;
    }

    private Flowable<List<Station>> initFavorites() {
        return dao.getStations()
                .map(stationEntities -> {
                    List<Station> stations = new ArrayList<>(stationEntities.size());
                    for (StationEntity stationEntity : stationEntities) {
                        stations.add(new Station(stationEntity));
                    }
                    return stations;
                }).subscribeOn(Schedulers.io())
                .doOnNext(stations -> favoriteStations.accept(stations));
    }
}
