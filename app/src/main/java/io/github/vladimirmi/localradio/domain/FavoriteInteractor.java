package io.github.vladimirmi.localradio.domain;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoriteInteractor {

    private final StationsRepository stationsRepository;
    private final FavoriteRepository favoriteRepository;

    @Inject
    public FavoriteInteractor(StationsRepository stationsRepository,
                              FavoriteRepository favoriteRepository) {
        this.stationsRepository = stationsRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public Completable initFavorites() {
        return favoriteRepository.initFavorites()
                .doOnComplete(() -> {
                    updateStationsWithFavorites();
                    setCurrentStationIfFavorite();
                });
    }

    public void setFavorites(List<Station> stations) {
        favoriteRepository.setFavoriteStations(stations);
        updateStationsWithFavorites();
        setCurrentStationIfFavorite();
    }

    public Completable switchCurrentFavorite() {
        return Single.fromCallable(stationsRepository::getCurrentStation)
                .flatMapCompletable(station -> {
                    Station newStation = station.copy(!station.isFavorite());
                    stationsRepository.setCurrentStation(newStation);
                    if (newStation.isFavorite()) {
                        return favoriteRepository.addFavorite(newStation);
                    } else {
                        return favoriteRepository.removeFavorite(newStation);
                    }
                }).subscribeOn(Schedulers.io());
    }

    public void previousStation() {
        List<Station> source = favoriteRepository.getFavoriteStations();
        int indexOfCurrent = source.indexOf(stationsRepository.getCurrentStation());
        if (indexOfCurrent == -1) return;

        int indexOfPrevious = (indexOfCurrent + source.size() - 1) % source.size();
        stationsRepository.setCurrentStation(source.get(indexOfPrevious));
    }

    public void nextStation() {
        List<Station> source = favoriteRepository.getFavoriteStations();
        int indexOfCurrent = source.indexOf(stationsRepository.getCurrentStation());
        if (indexOfCurrent == -1) return;

        int indexOfNext = (indexOfCurrent + 1) % source.size();
        stationsRepository.setCurrentStation(source.get(indexOfNext));
    }

    public void updateStationsWithFavorites() {
        List<Station> list = new ArrayList<>(stationsRepository.getStations());
        boolean updated = updateStationsIfFavorite(list);
        if (updated) stationsRepository.setStations(list);
    }

    private void setCurrentStationIfFavorite() {
        Station currentFavoriteStation = favoriteRepository.findCurrentFavoriteStation();
        if (currentFavoriteStation != null) {
            stationsRepository.setCurrentStation(currentFavoriteStation);
        }
    }

    private boolean updateStationsIfFavorite(List<Station> stations) {
        boolean updated = false;
        for (int i = 0; i < stations.size(); i++) {
            Station station = stations.get(i);
            boolean isFavorite = false;
            for (Station favoriteStation : favoriteRepository.getFavoriteStations()) {
                if (station.getId() == favoriteStation.getId()) {
                    isFavorite = true;
                    break;
                }
            }
            if (station.isFavorite() != isFavorite) {
                stations.set(i, station.copy(isFavorite));
                updated = true;
            }
        }
        return updated;
    }
}
