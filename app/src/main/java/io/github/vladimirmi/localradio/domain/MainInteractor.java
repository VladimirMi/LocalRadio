package io.github.vladimirmi.localradio.domain;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */
public class MainInteractor {

    private final Preferences preferences;
    private final FavoriteInteractor favoriteInteractor;
    private final SearchInteractor searchInteractor;

    @Inject
    public MainInteractor(Preferences preferences,
                          FavoriteInteractor favoriteInteractor,
                          SearchInteractor searchInteractor) {
        this.preferences = preferences;
        this.favoriteInteractor = favoriteInteractor;
        this.searchInteractor = searchInteractor;
    }

    public Completable initApp() {
        Timber.e("initApp: ");
        Completable initStations;
        if (searchInteractor.isSearchDone()) {
            initStations = searchInteractor.checkCanSearch()
                    .doOnComplete(searchInteractor::searchStations)
                    .andThen(searchInteractor.getSearchResults())
                    .doOnNext(integer -> Timber.e("initApp: " + integer))
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
