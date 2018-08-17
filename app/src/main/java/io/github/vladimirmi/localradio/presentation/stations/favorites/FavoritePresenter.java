package io.github.vladimirmi.localradio.presentation.stations.favorites;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.interactors.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlsInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.stations.base.BaseStationsPresenter;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoritePresenter extends BaseStationsPresenter {

    private final FavoriteInteractor favoriteInteractor;

    @Inject
    FavoritePresenter(StationsInteractor stationsInteractor,
                      FavoriteInteractor favoriteInteractor,
                      PlayerControlsInteractor controlInteractor) {
        super(stationsInteractor, controlInteractor);
        this.favoriteInteractor = favoriteInteractor;
    }

    @Override
    protected Observable<List<Station>> getStations() {
        return favoriteInteractor.getFavoriteStations();
    }

    @Override
    protected void handleStations(List<Station> stations) {
        view.setStations(stations);
        if (stations.isEmpty()) {
            view.showPlaceholder();
        } else {
            view.hidePlaceholder();
        }
    }
}
