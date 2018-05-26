package io.github.vladimirmi.localradio.presentation.stations;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.PlayerControlInteractor;
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

    @Inject
    StationsPresenter(StationsInteractor stationsInteractor,
                      PlayerControlInteractor controlInteractor,
                      SearchInteractor searchInteractor) {
        super(stationsInteractor, controlInteractor);
        this.searchInteractor = searchInteractor;
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
