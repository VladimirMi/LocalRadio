package io.github.vladimirmi.localradio.presentation.main;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private final PlayerControlInteractor controlInteractor;
    private final StationsInteractor stationsInteractor;

    @Inject
    public MainPresenter(PlayerControlInteractor controlInteractor,
                         StationsInteractor stationsInteractor) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
    }

    @Override
    protected void onAttach(MainView view) {
        controlInteractor.connect();
        view.showStations();
        compDisp.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleCurrentStation));
    }

    @Override
    protected void onDetach() {
        controlInteractor.disconnect();
    }

    private void handleCurrentStation(Station station) {
        if (station.isNullStation()) {
            view.hideControls();
        } else {
            view.showControls();
        }
    }
}
