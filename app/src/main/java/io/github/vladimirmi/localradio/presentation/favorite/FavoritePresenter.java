package io.github.vladimirmi.localradio.presentation.favorite;

import android.support.annotation.Nullable;
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
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private final StationsInteractor stationsInteractor;
    private final FavoriteInteractor favoriteInteractor;
    private final PlayerControlInteractor controlInteractor;

    @Inject
    FavoritePresenter(StationsInteractor stationsInteractor,
                      FavoriteInteractor favoriteInteractor,
                      PlayerControlInteractor controlInteractor) {
        this.stationsInteractor = stationsInteractor;
        this.favoriteInteractor = favoriteInteractor;
        this.controlInteractor = controlInteractor;
    }

    @Override
    protected void onFirstAttach(@Nullable FavoriteView view, CompositeDisposable disposables) {
        disposables.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        if (view != null) view.selectStation(station);
                    }
                }));

        disposables.add(controlInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<PlaybackStateCompat>(view) {
                    @Override
                    public void onNext(PlaybackStateCompat state) {
                        if (view != null) {
                            view.setSelectedPlaying(state.getState() == PlaybackStateCompat.STATE_PLAYING);
                        }
                    }
                }));
    }


    public void listChanged(List<Station> list) {
        favoriteInteractor.setFavorites(list);
    }

    public void selectStation(Station station) {
        stationsInteractor.setCurrentStation(station);
    }

}
