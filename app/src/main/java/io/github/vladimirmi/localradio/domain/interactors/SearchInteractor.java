package io.github.vladimirmi.localradio.domain.interactors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
import io.github.vladimirmi.localradio.domain.repositories.StationsRepository;
import io.github.vladimirmi.localradio.map.MapState;
import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchInteractor {

    private final StationsRepository stationsRepository;
    private final NetworkChecker networkChecker;
    private final LocationRepository locationRepository;
    private final SearchRepository searchRepository;

    @Inject
    public SearchInteractor(StationsRepository stationsRepository,
                            NetworkChecker networkChecker,
                            LocationRepository locationRepository,
                            SearchRepository searchRepository) {
        this.stationsRepository = stationsRepository;
        this.networkChecker = networkChecker;
        this.locationRepository = locationRepository;
        this.searchRepository = searchRepository;
    }

    public void saveSearchMode(int mode) {
        searchRepository.saveSearchMode(mode);
    }

    public int getSearchMode() {
        return searchRepository.getSearchMode();
    }

    public Observable<SearchResult> getSearchResultObs() {
        return searchRepository.searchResult();
    }

    public SearchResult getSearchState() {
        return searchRepository.searchResult().blockingFirst();
    }

    public void resetSearch() {
        searchRepository.setSearchResult(SearchResult.notDone());
        stationsRepository.resetStations();
    }

    public Completable searchStations() {
        return search(false);
    }

    public Completable refreshStations() {
        return search(true);
    }

    private Completable search(boolean skipCache) {
        searchRepository.setSkipCache(skipCache);
        stationsRepository.resetStations();

        Single<List<Station>> search = locationRepository.getSavedLocations()
                .filter(locations -> !locations.isEmpty())
                .toSingle()
                .flatMap(locations -> {
                    if (locationRepository.getMapMode().equals(MapWrapper.RADIUS_MODE) &&
                            allIsUS(locations)) {
                        return searchStationsByCoordinates();
                    } else {
                        return searchStationsByLocations(locations);
                    }
                });

        return checkCanSearch()
                .doOnComplete(() -> searchRepository.setSearchResult(SearchResult.loading()))
                .andThen(search)
                .doOnSuccess(stations -> {
                    searchRepository.setSearchResult(SearchResult.done(stations.size()));
                    stationsRepository.setSearchResult(stations);
                })
                .doOnError(throwable -> searchRepository.setSearchResult(SearchResult.notDone()))
                .toCompletable();
    }

    private boolean allIsUS(List<LocationEntity> locations) {
        for (LocationEntity location : locations) {
            if (!location.country.equals("US")) return false;
        }
        return true;
    }

    private Single<List<Station>> searchStationsByCoordinates() {
        MapState mapState = locationRepository.getMapState();
        return searchRepository.searchStationsByCoordinates(mapState);
    }


    private Single<List<Station>> searchStationsByLocations(List<LocationEntity> locations) {
        return Observable.fromIterable(locations)
                .flatMapSingle(location -> {
                    if (location.isCountry()) {
                        return searchRepository.searchStationsByCountry(location.country);
                    } else {
                        return Observable.fromIterable(Arrays.asList(location.endpoints.split(",")))
                                .flatMapSingle(city -> {
                                    return searchRepository.searchStationsByCity(location.country, city.trim());
                                })
                                .<List<Station>>collect(ArrayList::new, List::addAll);
                    }
                })
                .collect(ArrayList::new, List::addAll);
    }

    private Completable checkCanSearch() {
        if (!networkChecker.isAvailableNet()) {
            return Completable.error(new MessageException(R.string.error_network));
        } else {
            return Completable.complete();
        }
    }
}
