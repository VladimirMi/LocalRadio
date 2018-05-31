package io.github.vladimirmi.localradio.domain.interactors;

import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.source.LocationSource;
import io.github.vladimirmi.localradio.domain.models.SearchState;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
import io.github.vladimirmi.localradio.domain.repositories.StationsRepository;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

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

    public Observable<SearchState> getSearchStateObs() {
        return searchRepository.searchState();
    }

    public SearchState getSearchState() {
        return searchRepository.searchState().blockingFirst();
    }

    public void resetSearch() {
        searchRepository.setSearchState(SearchState.NOT_DONE);
        stationsRepository.resetStations();
    }

    public Completable searchStations() {
        resetSearch();
        return search(false).subscribeOn(Schedulers.io());
    }

    public Completable refreshStations() {
        resetSearch();
        return search(true).subscribeOn(Schedulers.io());
    }

    private Completable search(boolean skipCache) {
        searchRepository.setSkipCache(skipCache);
        searchRepository.setSearchState(SearchState.LOADING);
        Single<List<Station>> search;

        if (locationRepository.isAutodetect()) {
            search = locationRepository.checkCanGetLocation()
                    .andThen(searchStationsAuto());
        } else {
            search = searchStationsManual();
        }
        return search
                .doOnSuccess(stationsRepository::setSearchResult)
                .toCompletable();
    }

    private Single<List<Station>> searchStationsAuto() {
        return locationRepository.getCoordinates()
                .flatMap(coordinates -> {
                    Pair<String, String> countryCodeCity = locationRepository.getCountryCodeCity(coordinates);

                    if (countryCodeCity == null) {
                        return searchStationsByIp();
                    }
                    locationRepository.saveCountryCodeCity(countryCodeCity);

                    if (countryCodeCity.first.equals("US")) {
                        return searchRepository.searchStationsByCoordinates(coordinates);
                    } else {
                        return searchRepository.searchStationsManual(
                                locationRepository.getCountryCode(),
                                locationRepository.getCity());
                    }
                }).onErrorResumeNext(throwable -> {
                    if (throwable instanceof LocationSource.LocationTimeoutException) {
                        return searchStationsByIp();
                    } else {
                        return Single.error(throwable);
                    }
                });
    }

    private Single<List<Station>> searchStationsManual() {
        return searchRepository.searchStationsManual(locationRepository.getCountryCode(),
                locationRepository.getCity());
    }

    private Single<List<Station>> searchStationsByIp() {
        return searchRepository.searchStationsByIp()
                .doOnSuccess(stations -> {
                    if (!stations.isEmpty()) {
                        locationRepository.saveCountryCodeCity(stations.get(0).countryCode, "");
                    }
                });
    }

    public Completable checkCanSearch() {
        if (!networkChecker.isAvailableNet()) {
            return Completable.error(new MessageException(R.string.error_network));
        } else {
            return Completable.complete();
        }
    }
}
