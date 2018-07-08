package io.github.vladimirmi.localradio.presentation.search;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 01.07.2018.
 */
@SuppressWarnings("WeakerAccess")
public class SearchPresenter extends BasePresenter<SearchView> {

    public static final int MAP_MODE = 0;
    public static final int MANUAL_MODE = 1;

    private final SearchInteractor searchInteractor;

    @Inject
    public SearchPresenter(SearchInteractor searchInteractor) {
        this.searchInteractor = searchInteractor;
    }

    @Override
    protected void onFirstAttach(SearchView view, CompositeDisposable disposables) {
        view.setSearchMode(searchInteractor.getSearchMode());
    }

    public void setSearchMode(int mode) {
        searchInteractor.saveSearchMode(mode);
    }
}
