package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
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

    public boolean isSearchDone() {
        return stationsRepository.isSearchDone();
    }

    public void resetSearch() {
        stationsRepository.resetStations();
        stationsRepository.setSearchDone(false);
    }

    public Single<List<Station>> searchStations() {
        resetSearch();
        return performSearch(false);
    }

    public Single<List<Station>> refreshStations() {
        stationsRepository.resetStations();
        return performSearch(true);
    }

    private Single<List<Station>> performSearch(boolean skipCache) {
        Single<List<Station>> search;

        if (locationRepository.isAutodetect()) {
            search = searchStationsAuto(skipCache);
        } else {
            search = searchStationsManual(skipCache);
        }

        return search.doOnSuccess(stations -> {
            favoriteRepository.updateStationsIfFavorite(stations);
            stationsRepository.updateCurrentStationFromPreferences(stations);
            stationsRepository.stations.accept(stations);
            stationsRepository.setSearchDone(true);
        });
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
