package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchInteractor {

    private final StationsRepository stationsRepository;
    private final LocationRepository locationRepository;
    private final FavoriteRepository favoriteRepository;

    @Inject
    public SearchInteractor(StationsRepository stationsRepository,
                            LocationRepository locationRepository,
                            FavoriteRepository favoriteRepository) {
        this.stationsRepository = stationsRepository;
        this.locationRepository = locationRepository;
        this.favoriteRepository = favoriteRepository;
    }

    public boolean isCanSearch() {
        return stationsRepository.isCanSearch();
    }

    public Completable searchStations() {
        return performSearch(false);
    }

    public Completable refreshStations() {
        return performSearch(true);
    }

    private Completable performSearch(boolean skipCache) {
        Single<List<Station>> search;

        if (locationRepository.isAutodetect()) {
            search = searchStationsAuto(skipCache);
        } else {
            search = searchStationsManual(skipCache);
        }

        return search
                .doOnError(e -> stationsRepository.setCanSearch(false))
                .doOnSuccess(stations -> {
                    favoriteRepository.updateStationsIfFavorite(stations);
                    stationsRepository.updateCurrentStationFromPreferences(stations);
                    stationsRepository.stations.accept(stations);
                    stationsRepository.setCanSearch(true);
                }).toCompletable();
    }

    private Single<List<Station>> searchStationsManual(boolean skipCache) {
        return stationsRepository.searchStationsManual(skipCache);
    }

    private Single<List<Station>> searchStationsAuto(boolean skipCache) {
        return stationsRepository.searchStationsAuto(skipCache)
                .doOnSuccess(stations -> {
                    // TODO: 4/24/18 save more specific location
                    locationRepository.saveCountryCodeCity(stations.get(0).getCountryCode(), "");
                });
    }
}
