package io.github.vladimirmi.localradio.presentation.stations;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlsInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.SearchResult;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsPresenter extends BaseStationsPresenter {

    private final FavoriteInteractor favoriteInteractor;
    private final SearchInteractor searchInteractor;

    @Inject
    StationsPresenter(StationsInteractor stationsInteractor,
                      PlayerControlsInteractor controlInteractor,
                      FavoriteInteractor favoriteInteractor, SearchInteractor searchInteractor) {
        super(stationsInteractor, controlInteractor);
        this.favoriteInteractor = favoriteInteractor;
        this.searchInteractor = searchInteractor;
    }

    @Override
    protected void onAttach(StationsView view) {
        super.onAttach(view);

        viewSubs.add(favoriteInteractor.getFavoriteIds()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Set<Integer>>(view) {
                    @Override
                    public void onNext(Set<Integer> ids) {
                        view.setFavorites(ids);
                    }
                }));

        viewSubs.add(searchInteractor.getSearchResultObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<SearchResult>(view) {
                    @Override
                    public void onNext(SearchResult result) {
                        handleSearchResult(result);
                    }
                }));
    }

    @Override
    protected Observable<List<Station>> getStations() {
        return stationsInteractor.getFilteredStationsObs();
    }

    private void handleSearchResult(SearchResult result) {
        if (result.state == SearchResult.State.LOADING) {
            view.setSearching(true);
            view.hidePlaceholder();
        } else {
            view.setSearching(false);
            if (result.result == 0) {
                view.showPlaceholder();
            } else {
                view.hidePlaceholder();
            }
        }
    }

    @Override
    protected void handleStations(List<Station> stations) {
        view.setStations(stations);
    }

    public void filterStations(String filter) {
        stationsInteractor.filterStations(filter);
    }

    public String getFilter() {
        return stationsInteractor.getFilter();
    }
}
