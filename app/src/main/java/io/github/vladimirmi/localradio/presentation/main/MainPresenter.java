package io.github.vladimirmi.localradio.presentation.main;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
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
    private final Preferences preferences;

    @Inject
    public MainPresenter(PlayerControlInteractor controlInteractor,
                         StationsInteractor stationsInteractor,
                         Preferences preferences) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
        this.preferences = preferences;
    }

    @Override
    protected void onAttach(MainView view) {
        controlInteractor.connect();

        initPage();

        compDisp.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleCurrentStation));
    }

    @Override
    protected void onDetach() {
        controlInteractor.disconnect();
    }

    public void selectPage(int position) {
        preferences.page.put(position);
        initPage();
    }

    private void handleCurrentStation(Station station) {
        if (station.isNullStation()) {
            view.hideControls();
        } else {
            view.showControls();
        }
    }

    private void initPage() {
        switch (preferences.page.get()) {
            case 0:
                view.showFavorite();
                break;
            case 1:
                view.showStations();
                break;
            case 2:
                view.showSearch();
                break;
        }
    }
}
