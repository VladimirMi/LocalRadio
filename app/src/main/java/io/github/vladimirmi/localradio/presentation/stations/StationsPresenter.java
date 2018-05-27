package io.github.vladimirmi.localradio.presentation.stations;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlsInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsPresenter extends BaseStationsPresenter {

    private final SearchInteractor searchInteractor;
    private final FavoriteInteractor favoriteInteractor;

    @Inject
    StationsPresenter(StationsInteractor stationsInteractor,
                      PlayerControlsInteractor controlInteractor,
                      SearchInteractor searchInteractor,
                      FavoriteInteractor favoriteInteractor) {
        super(stationsInteractor, controlInteractor);
        this.searchInteractor = searchInteractor;
        this.favoriteInteractor = favoriteInteractor;
    }

    @Override
    protected void onAttach(StationsView view, boolean isFirstAttach) {
        super.onAttach(view, isFirstAttach);

        disposables.add(searchInteractor.isSearching()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isSearching) {
                        if (isSearching) view.hidePlaceholder();
                        view.setSearching(isSearching);
                    }
                }));

        disposables.add(favoriteInteractor.getFavoriteIds()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Set<Integer>>(view) {
                    @Override
                    public void onNext(Set<Integer> ids) {
                        view.setFavorites(ids);
                    }
                }));
    }

    @Override
    protected Observable<List<Station>> getStations() {
        return stationsInteractor.getStationsObs();
    }

    public void filterStations(String filter) {
        stationsInteractor.filterStations(filter);
    }

    public String getFilter() {
        return stationsInteractor.getFilter();
    }
}
