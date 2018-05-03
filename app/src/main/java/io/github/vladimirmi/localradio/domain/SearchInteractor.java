package io.github.vladimirmi.localradio.domain;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.data.service.search.SearchService;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class SearchInteractor {

    private final StationsRepository stationsRepository;
    private final NetworkChecker networkChecker;

    @Inject
    public SearchInteractor(StationsRepository stationsRepository,
                            NetworkChecker networkChecker) {
        this.stationsRepository = stationsRepository;
        this.networkChecker = networkChecker;
    }

    public boolean isSearchDone() {
        return stationsRepository.isSearchDone();
    }

    public void resetSearch() {
        stationsRepository.resetSearch();
    }

    public void searchStations() {
        SearchService.performSearch(false);
    }

    public void refreshStations() {
        resetSearch();
        SearchService.performSearch(true);
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
}
