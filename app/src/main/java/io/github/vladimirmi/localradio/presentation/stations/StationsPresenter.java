package io.github.vladimirmi.localradio.presentation.stations;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
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

        compDisp.add(interactor.getCurrentStationObs()
                .buffer(2, 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObservableObserver<List<Station>>(view) {
                    @Override
                    public void onNext(List<Station> stations) {
                        view.selectStation(stations);
                    }
                }));
    }

    public void selectStation(Station station) {
        compDisp.add(interactor.setCurrentStation(station)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }
}
