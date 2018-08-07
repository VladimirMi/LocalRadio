package io.github.vladimirmi.localradio.presentation.search;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 01.07.2018.
 */
@SuppressWarnings("WeakerAccess")
public class SearchPresenter extends BasePresenter<SearchView> {

    public static final int MAP_MODE = 0;
    public static final int MANUAL_MODE = 1;

    private final SearchInteractor searchInteractor;
    private final LocationInteractor locationInteractor;

    @Inject
    public SearchPresenter(SearchInteractor searchInteractor, LocationInteractor locationInteractor) {
        this.searchInteractor = searchInteractor;
        this.locationInteractor = locationInteractor;
    }

    @Override
    protected void onFirstAttach(SearchView view, CompositeDisposable disposables) {
        view.setSearchMode(searchInteractor.getSearchMode());
    }

    @Override
    protected void onAttach(SearchView view) {
        viewSubs.add(searchInteractor.getSearchResultObs()
                .skip(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<SearchResult>(view) {
                    @Override
                    public void onNext(SearchResult result) {
                        handleSearchResult(result);
                    }
                }));
    }

    public void setSearchMode(int mode) {
        searchInteractor.saveSearchMode(mode);
    }

    public void search() {
        if (searchInteractor.getSearchMode() == MAP_MODE) {
            locationInteractor.saveMapSelection();
        } else {
            locationInteractor.saveManualSelection();
        }

        dataSubs.add(searchInteractor.searchStations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(getView())));
    }

    private void handleSearchResult(SearchResult result) {
        if (result.state == SearchResult.State.LOADING) {
            view.showLoading(true);
        } else if (result.state == SearchResult.State.DONE) {
            view.showLoading(false);
            view.setSearchResult(result.result);
        } else {
            view.showLoading(false);
            Timber.e("handleSearchResult: not done");
        }
    }
}
