package io.github.vladimirmi.localradio.domain;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */
public class MainInteractor {

    private final Preferences preferences;
    private final FavoriteInteractor favoriteInteractor;
    private final SearchInteractor searchInteractor;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public MainInteractor(Preferences preferences,
                          FavoriteInteractor favoriteInteractor,
                          SearchInteractor searchInteractor) {
        this.preferences = preferences;
        this.favoriteInteractor = favoriteInteractor;
        this.searchInteractor = searchInteractor;
    }

    public Completable initApp() {
        Completable initStations;
        if (searchInteractor.isSearchDone()) {
            initStations = searchInteractor.checkCanSearch()
                    .doOnComplete(searchInteractor::searchStations)
                    .andThen(searchInteractor.getSearchResults())
                    .firstOrError().toCompletable();
        } else {
            initStations = Completable.complete();
        }
        return Completable.mergeArrayDelayError(
                initStations,
                favoriteInteractor.initFavorites()
        ).subscribeOn(Schedulers.io());
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
}
