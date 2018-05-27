package io.github.vladimirmi.localradio.domain.interactors;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.repositories.LocationRepositoryImpl;
import io.github.vladimirmi.localradio.data.repositories.StationsRepositoryImpl;
import io.github.vladimirmi.localradio.data.service.search.SearchService;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchInteractor {

    private final StationsRepositoryImpl stationsRepository;
    private final NetworkChecker networkChecker;
    private final LocationRepositoryImpl locationRepository;

    @Inject
    public SearchInteractor(StationsRepositoryImpl stationsRepository,
                            NetworkChecker networkChecker, LocationRepositoryImpl locationRepository) {
        this.stationsRepository = stationsRepository;
        this.networkChecker = networkChecker;
        this.locationRepository = locationRepository;
    }

    public boolean isSearchDone() {
        return stationsRepository.isSearchDone();
    }

    public Observable<Boolean> isSearching() {
        return stationsRepository.isSearching();
    }

    public void resetSearch() {
        stationsRepository.resetSearch();
    }

    public Completable searchStations() {
        return performSearch(false);
    }

    public Completable refreshStations() {
        resetSearch();
        return performSearch(true);
    }

    public Completable checkCanSearch() {
        if (!networkChecker.isAvailableNet()) {
            return Completable.error(new MessageException(R.string.error_network));
        } else {
            return Completable.complete();
        }
    }

    public Observable<Integer> getSearchResults() {
        return stationsRepository.getStationsObs()
                .map(List::size)
                .filter(size -> isSearchDone());
    }

    private Completable performSearch(boolean skipCache) {
        Completable checks;
        if (locationRepository.isAutodetect()) {
            checks = locationRepository.checkCanGetLocation();
        } else {
            checks = Completable.complete();
        }
        return checks.andThen(Completable.fromRunnable(() -> SearchService.performSearch(skipCache)));
    }
}
