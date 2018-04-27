package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.utils.MessageException;
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

    public Single<List<Station>> searchStations() {
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

        return search
                .doOnError(e -> stationsRepository.setCanSearch(false))
                .doOnSuccess(stations -> {
                    favoriteRepository.updateStationsIfFavorite(stations);
                    stationsRepository.updateCurrentStationFromPreferences(stations);
                    stationsRepository.stations.accept(stations);
                    stationsRepository.setCanSearch(true);
                });
    }

    private Single<List<Station>> searchStationsManual(boolean skipCache) {
        String countryCode = locationRepository.getCountryCode();
        String city = locationRepository.getCity();
        if (countryCode.isEmpty() && city.isEmpty()) {
            return Single.error(new MessageException(R.string.error_specify_location));
        }
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
