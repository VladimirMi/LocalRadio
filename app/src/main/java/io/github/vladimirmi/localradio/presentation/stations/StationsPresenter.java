package io.github.vladimirmi.localradio.presentation.stations;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlsInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.SearchState;
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

        disposables.add(favoriteInteractor.getFavoriteIds()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Set<Integer>>(view) {
                    @Override
                    public void onNext(Set<Integer> ids) {
                        view.setFavorites(ids);
                    }
                }));

        disposables.add(searchInteractor.getSearchStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<SearchState>(view) {
                    @Override
                    public void onNext(SearchState state) {
                        view.setSearching(state == SearchState.LOADING);
                        if (state == SearchState.LOADING) view.hidePlaceholder();
                    }
                }));
    }

    @Override
    protected Observable<List<Station>> getStations() {
        return stationsInteractor.getFilteredStationsObs();
    }

    @Override
    protected void handleStations(List<Station> stations) {
        view.setStations(stations);
        if (stations.isEmpty() && searchInteractor.getSearchState() != SearchState.LOADING) {
            view.showPlaceholder();
        } else {
            view.hidePlaceholder();
        }
    }

    public void filterStations(String filter) {
        stationsInteractor.filterStations(filter);
    }

    public String getFilter() {
        return stationsInteractor.getFilter();
    }
}
