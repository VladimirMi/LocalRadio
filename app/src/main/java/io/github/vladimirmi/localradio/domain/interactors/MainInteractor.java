package io.github.vladimirmi.localradio.domain.interactors;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */
public class MainInteractor {

    private final Preferences preferences;
    private final SearchInteractor searchInteractor;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public MainInteractor(Preferences preferences,
                          SearchInteractor searchInteractor) {
        this.preferences = preferences;
        this.searchInteractor = searchInteractor;
    }

    public Completable initApp() {
        Completable initStations;
        if (searchInteractor.isSearchDone()) {
            initStations = searchInteractor.checkCanSearch()
                    .andThen(searchInteractor.searchStations());
        } else {
            initStations = Completable.complete();
        }
        return initStations.subscribeOn(Schedulers.io());
    }

    public int getPagePosition() {
        return preferences.pagePosition.get();
    }

    public void savePagePosition(int position) {
        preferences.pagePosition.put(position);
    }

    public boolean isFavoritePage() {
        return getPagePosition() == 0;
    }

    public boolean isSearchPage() {
        return getPagePosition() == 2;
    }

    public boolean isHaveStations() {
        return preferences.currentStationId.get() != 0;
    }
}
