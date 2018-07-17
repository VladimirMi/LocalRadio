package io.github.vladimirmi.localradio.domain.interactors;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
import io.github.vladimirmi.localradio.domain.repositories.StationsRepository;
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

        Single<List<Station>> search;

        if (locationRepository.isAutodetect()) {
            search = locationRepository.checkCanGetLocation()
                    .andThen(searchStationsAuto());
        } else {
            search = searchStationsManual();
        }
        return checkCanSearch()
                .doOnComplete(() -> searchRepository.setSearchResult(SearchResult.loading()))
                .andThen(search)
                .doOnSuccess(stationsRepository::setSearchResult)
                .doOnError(throwable -> searchRepository.setSearchResult(SearchResult.notDone()))
                .toCompletable();
    }

    private Single<List<Station>> searchStationsAuto() {
        return Single.just(Collections.emptyList());
//        return locationRepository.getCoordinates()
//                .flatMap(coordinates -> {
//                    Pair<String, String> countryCodeCity =
//                            locationRepository.getCountryCodeCity(coordinates);
//
//                    if (countryCodeCity == null) {
//                        return searchStationsByIp();
//                    }
//                    locationRepository.saveCountryCodeCity(countryCodeCity.first,
//                            countryCodeCity.second);
//
//                    if (countryCodeCity.first.equals("US")) {
//                        return searchRepository.searchStationsByCoordinates(coordinates);
//                    } else {
//                        return searchRepository.searchStationsAutoManual(countryCodeCity.first,
//                                countryCodeCity.second);
//                    }
//                }).onErrorResumeNext(throwable -> {
//                    if (throwable instanceof LocationSource.LocationTimeoutException) {
//                        return searchStationsByIp();
//                    } else {
//                        return Single.error(throwable);
//                    }
//                });
    }

    private Single<List<Station>> searchStationsManual() {
        return Single.just(Collections.emptyList());
//        return searchRepository.searchStationsManual(locationRepository.getCountryCode(),
//                locationRepository.getCity());
    }

    private Single<List<Station>> searchStationsByIp() {
        return Single.just(Collections.emptyList());
//        return searchRepository.searchStationsByIp()
//                .doOnSuccess(stations -> {
//                    if (!stations.isEmpty()) {
//                        locationRepository.saveCountryCodeCity(stations.get(0).countryCode, "");
//                    }
//                });
    }

    private Completable checkCanSearch() {
        if (!networkChecker.isAvailableNet()) {
            return Completable.error(new MessageException(R.string.error_network));
        } else {
            return Completable.complete();
        }
    }
}
