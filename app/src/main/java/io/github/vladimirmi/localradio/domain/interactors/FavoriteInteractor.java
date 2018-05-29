package io.github.vladimirmi.localradio.domain.interactors;

import android.annotation.SuppressLint;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.repositories.StationsRepositoryImpl;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.FavoriteRepository;
import io.github.vladimirmi.localradio.domain.repositories.PlayerController;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteInteractor {

    private final StationsRepositoryImpl stationsRepository;
    private final FavoriteRepository favoriteRepository;
    private final PlayerController controller;
    private final MainInteractor mainInteractor;

    @SuppressLint("CheckResult")
    @Inject
    public FavoriteInteractor(StationsRepositoryImpl stationsRepository,
                              FavoriteRepository favoriteRepository,
                              PlayerController controller,
                              MainInteractor mainInteractor) {
        this.stationsRepository = stationsRepository;
        this.favoriteRepository = favoriteRepository;
        this.controller = controller;
        this.mainInteractor = mainInteractor;

        getFavoriteStations()
                .firstElement()
                .subscribe(this::initCurrentStation);
    }

    public Observable<List<Station>> getFavoriteStations() {
        return favoriteRepository.getFavoriteStationsObs();
    }

    public Observable<Set<Integer>> getFavoriteIds() {
        return getFavoriteStations()
                .map(stations -> {
                    Set<Integer> ids = new HashSet<>(stations.size());
                    for (Station station : stations) {
                        ids.add(station.id);
                    }
                    return ids;
                });
    }

    public Completable switchCurrentFavorite() {
        Completable switchFavorite;
        Station current = stationsRepository.getCurrentStation();
        if (favoriteRepository.getFavoriteStations().contains(current)) {
            changeCurrentStationOnNextFavorite();
            switchFavorite = favoriteRepository.removeFavorite(current);
        } else {
            switchFavorite = favoriteRepository.addFavorite(current);
        }

        return switchFavorite.subscribeOn(Schedulers.io());
    }

    public Observable<Boolean> isCurrentStationFavorite() {
        return Observable.combineLatest(getFavoriteStations(),
                stationsRepository.getCurrentStationObs(),
                List::contains)
                .doOnNext(favoriteRepository::setCurrentStationIsFavorite);
    }

    public void previousStation() {
        List<Station> source = favoriteRepository.getFavoriteStations();
        int indexOfCurrent = getIndexOfCurrent();
        if (indexOfCurrent == -1) return;

        int indexOfPrevious = (indexOfCurrent + source.size() - 1) % source.size();
        stationsRepository.setCurrentStation(source.get(indexOfPrevious));
    }

    public void nextStation() {
        List<Station> source = favoriteRepository.getFavoriteStations();
        int indexOfCurrent = getIndexOfCurrent();
        if (indexOfCurrent == -1) return;

        int indexOfNext = (indexOfCurrent + 1) % source.size();
        stationsRepository.setCurrentStation(source.get(indexOfNext));
    }


    private void changeCurrentStationOnNextFavorite() {
        if (!mainInteractor.isFavoritePage() || controller.isPlaying()) {
            return;
        }
        if (favoriteRepository.getFavoriteStations().size() == 1) {
            if (stationsRepository.getStations().isEmpty()) {
                stationsRepository.setCurrentStation(Station.nullObject());
            }
            return;
        }
        if (getIndexOfCurrent() + 1 == favoriteRepository.getFavoriteStations().size()) {
            previousStation();
        } else {
            nextStation();
        }
    }

    private int getIndexOfCurrent() {
        List<Station> stations = favoriteRepository.getFavoriteStations();
        Station currentStation = stationsRepository.getCurrentStation();

        for (int i = 0; i < stations.size(); i++) {
            if (stations.get(i).id == currentStation.id) {
                return i;
            }
        }
        return -1;
    }

    private void initCurrentStation(List<Station> stations) {
        int id = favoriteRepository.getCurrentFavoriteStationId();
        if (id == -1) return;
        for (Station station : stations) {
            if (station.id == id) {
                stationsRepository.setCurrentStation(station);
                break;
            }
        }
    }
}
