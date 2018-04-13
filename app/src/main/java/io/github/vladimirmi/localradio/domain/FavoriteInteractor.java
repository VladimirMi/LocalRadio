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
        stationsRepository.updateStations(stations.toArray(new Station[stations.size()]));
    }

    public Completable switchFavorite(Station station) {
        station.setFavorite(!station.isFavorite());
        Completable switchFavorite;

        if (station.isFavorite()) {
            switchFavorite = favoriteRepository.addFavorite(station);
        } else {
            switchFavorite = favoriteRepository.removeFavorite(station);
        }
        return switchFavorite.subscribeOn(Schedulers.io());
    }
}
