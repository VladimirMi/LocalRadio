package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

@SuppressWarnings("WeakerAccess")
public class SearchInteractor {

    private final StationsRepository stationsRepository;
    private final LocationRepository locationRepository;
    private final FavoriteRepository favoriteRepository;
    private final NetworkChecker networkChecker;

    @Inject
    public SearchInteractor(StationsRepository stationsRepository,
                            LocationRepository locationRepository,
                            FavoriteRepository favoriteRepository,
                            NetworkChecker networkChecker) {
        this.stationsRepository = stationsRepository;
        this.locationRepository = locationRepository;
        this.favoriteRepository = favoriteRepository;
        this.networkChecker = networkChecker;
    }

    public boolean isSearchDone() {
        return stationsRepository.isSearchDone();
    }

    public void resetSearch() {
        stationsRepository.resetStations();
        stationsRepository.setSearchDone(false);
    }

    public Single<List<Station>> searchStations(boolean resetSearch) {
        if (resetSearch) resetSearch();
        return performSearch(false);
    }

    public Single<List<Station>> refreshStations() {
        stationsRepository.resetStations();
        return performSearch(true);
    }

    public Completable checkCanSearch() {
        if (!networkChecker.isAvailableNet()) {
            return Completable.error(new MessageException(R.string.error_network));
        } else {
            return Completable.complete();
        }
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
        }).doOnError(throwable -> stationsRepository.setSearchDone(false));
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
