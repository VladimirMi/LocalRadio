package io.github.vladimirmi.localradio.presentation.main;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.SearchInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private final PlayerControlInteractor controlInteractor;
    private final SearchInteractor searchInteractor;

    @Inject
    public MainPresenter(PlayerControlInteractor controlInteractor,
                         SearchInteractor searchInteractor) {
        this.controlInteractor = controlInteractor;
        this.searchInteractor = searchInteractor;
    }

    @Override
    protected void onAttach(MainView view) {
        controlInteractor.connect();
        if (searchInteractor.isDone()) {
            view.showStations();
        } else {
            view.showSearch();
        }
    }

    @Override
    protected void onDetach() {
        controlInteractor.disconnect();
    }
}
