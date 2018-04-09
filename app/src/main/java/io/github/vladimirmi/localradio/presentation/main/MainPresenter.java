package io.github.vladimirmi.localradio.presentation.main;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class MainPresenter extends BasePresenter<MainView> {

    private final PlayerControlInteractor controlInteractor;

    @Inject
    public MainPresenter(PlayerControlInteractor controlInteractor) {
        this.controlInteractor = controlInteractor;
    }

    @Override
    protected void onAttach(MainView view) {
        controlInteractor.connect();
    }

    @Override
    protected void onDetach() {
        controlInteractor.disconnect();
    }
}
