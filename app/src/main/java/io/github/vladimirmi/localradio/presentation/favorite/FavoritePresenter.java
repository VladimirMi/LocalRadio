package io.github.vladimirmi.localradio.presentation.favorite;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private final StationsInteractor interactor;

    @Inject
    public FavoritePresenter(StationsInteractor interactor) {
        this.interactor = interactor;
    }
}
