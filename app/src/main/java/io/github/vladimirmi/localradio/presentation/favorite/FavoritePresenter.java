package io.github.vladimirmi.localradio.presentation.favorite;

import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.domain.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private final StationsInteractor stationsInteractor;
    private final FavoriteInteractor favoriteInteractor;
    private final PlayerControlInteractor controlInteractor;

    @Inject
    public FavoritePresenter(StationsInteractor stationsInteractor,
                             FavoriteInteractor favoriteInteractor,
                             PlayerControlInteractor controlInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.favoriteInteractor = favoriteInteractor;
        this.controlInteractor = controlInteractor;
    }

    @Override
    protected void onAttach(FavoriteView view) {
        compDisp.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObservableObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        view.selectStation(station);
                    }
                }));

        compDisp.add(controlInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObservableObserver<PlaybackStateCompat>(view) {
                    @Override
                    public void onNext(PlaybackStateCompat state) {
                        view.setSelectedPlaying(state.getState() == PlaybackStateCompat.STATE_PLAYING);
                    }
                }));
    }

    public void listChanged(List<Station> list) {
        favoriteInteractor.initFavorites(list);
    }

    public void selectStation(Station station) {
        compDisp.add(stationsInteractor.setCurrentStation(station)
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

}
