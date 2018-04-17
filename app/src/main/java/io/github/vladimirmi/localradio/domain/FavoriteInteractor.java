package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.reactivex.Completable;
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

    public void initFavorites(List<Station> stations) {
        stationsRepository.updateFavorites(stations);
        Station currentFavoriteStation = favoriteRepository.findCurrentFavoriteStation(stations);
        if (currentFavoriteStation != null) {
            stationsRepository.currentStation.accept(currentFavoriteStation);
        }
    }

    public Completable switchFavorite(Station station) {
        Station newStation = station.setFavorite(!station.isFavorite());
        Completable switchFavorite;

        if (newStation.isFavorite()) {
            switchFavorite = favoriteRepository.addFavorite(newStation);
        } else {
            switchFavorite = favoriteRepository.removeFavorite(newStation);
        }
        return switchFavorite.andThen(stationsRepository.setCurrentStation(newStation))
                .subscribeOn(Schedulers.io());
    }
}
