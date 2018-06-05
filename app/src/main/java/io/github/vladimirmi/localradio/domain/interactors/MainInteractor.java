package io.github.vladimirmi.localradio.domain.interactors;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.reactivex.Completable;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */
public class MainInteractor {

    // TODO: 5/30/18 create main repository
    private final Preferences preferences;
    private final SearchInteractor searchInteractor;
    private volatile boolean initialized;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public MainInteractor(Preferences preferences,
                          SearchInteractor searchInteractor) {
        this.preferences = preferences;
        this.searchInteractor = searchInteractor;
    }

    public Completable initApp() {
        Completable init;
        if (searchInteractor.getSearchState().isSearchDone() && !initialized) {
            init = searchInteractor.searchStations()
                    .doOnError(throwable -> initialized = false);
        } else {
            init = Completable.complete();
        }
        return init.doOnSubscribe(disposable -> initialized = true);
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
