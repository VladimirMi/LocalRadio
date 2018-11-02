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
import io.github.vladimirmi.localradio.map.MapPosition;
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
        return checkInternet()
                .andThen(search(false)).ignoreElement();
    }

    public Completable refreshStations() {
        return checkInternet()
                .andThen(search(true)).ignoreElement();
    }

    private Single<List<Station>> search(boolean skipCache) {
        return locationRepository.getSavedLocations()
                .flatMap(locations -> {
                    if (locationRepository.getMapMode().equals(MapWrapper.RADIUS_MODE) &&
                            allIsUS(locations)) {
                        return searchStationsByCoordinates();
                    } else {
                        return searchStationsByLocations(locations);
                    }
                })
                .doOnSubscribe(disposable -> {
                    searchRepository.setSkipCache(skipCache);
                    searchRepository.setSearchResult(SearchResult.loading());
                    stationsRepository.resetStations();
                })
                .doOnSuccess(stations -> {
                    searchRepository.setSearchResult(SearchResult.done(stations.size()));
                    stationsRepository.setSearchResult(stations);
                })
                .doOnError(throwable -> searchRepository.setSearchResult(SearchResult.notDone()));
    }

    private boolean allIsUS(List<LocationEntity> locations) {
        for (LocationEntity location : locations) {
            if (!location.country.equals("US")) return false;
        }
        return true;
    }

    private Single<List<Station>> searchStationsByCoordinates() {
        MapPosition mapState = locationRepository.getMapPosition();
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

    private Completable checkInternet() {
        return Completable.fromCallable(() -> {
            if (!networkChecker.isAvailableNet()) {
                throw new MessageException(R.string.error_network);
            }
            return null;
        });
    }
}
