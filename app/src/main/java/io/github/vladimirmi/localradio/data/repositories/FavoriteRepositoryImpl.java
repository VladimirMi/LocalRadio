package io.github.vladimirmi.localradio.data.repositories;

import com.jakewharton.rxrelay2.BehaviorRelay;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.AppDatabase;
import io.github.vladimirmi.localradio.data.db.StationEntity;
import io.github.vladimirmi.localradio.data.db.StationsDao;
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

    private Station findCurrentFavoriteStation() {
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
