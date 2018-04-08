package io.github.vladimirmi.localradio.presentation.stations;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsPresenter extends BasePresenter<StationsView> {

    private final StationsInteractor interactor;

    @Inject
    public StationsPresenter(StationsInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    protected void onAttach(StationsView view) {
        compDisp.add(interactor.getStationsObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setStations));
    }

    public void select(Station station) {
        interactor.setCurrentStation(station).subscribe();
    }
}
