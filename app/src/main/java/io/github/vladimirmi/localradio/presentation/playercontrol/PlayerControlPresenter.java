package io.github.vladimirmi.localradio.presentation.playercontrol;

import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.service.Metadata;
import io.github.vladimirmi.localradio.domain.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class PlayerControlPresenter extends BasePresenter<PlayerControlView> {

    private final PlayerControlInteractor controlInteractor;
    private final StationsInteractor stationsInteractor;
    private final FavoriteInteractor favoriteInteractor;


    @Inject
    PlayerControlPresenter(PlayerControlInteractor controlInteractor,
                           StationsInteractor stationsInteractor,
                           FavoriteInteractor favoriteInteractor) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
        this.favoriteInteractor = favoriteInteractor;
    }

    @Override
    protected void onFirstAttach(@Nullable PlayerControlView view, CompositeDisposable disposables) {
        disposables.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        handleCurrentStation(station);
                    }
                }));

        disposables.add(controlInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<PlaybackStateCompat>(view) {
                    @Override
                    public void onNext(PlaybackStateCompat playbackStateCompat) {
                        handleState(playbackStateCompat);
                    }
                }));

        disposables.add(controlInteractor.getPlaybackMetadataObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Metadata>(view) {
                    @Override
                    public void onNext(Metadata metadata) {
                        handleMetadata(metadata);
                    }
                }));
    }

    private void handleCurrentStation(Station station) {
        if (view == null) return;
        view.setStation(station);
    }

    private void handleState(PlaybackStateCompat state) {
        if (view == null) return;
        if (state.getState() == PlaybackStateCompat.STATE_PAUSED
                || state.getState() == PlaybackStateCompat.STATE_STOPPED) {
            view.showStopped();
        } else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            view.showPlaying();
        } else if (state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
            view.setMetadata(R.string.metadata_buffering);
        }
    }

    private void handleMetadata(Metadata metadata) {
        if (view == null) return;
        if (metadata.isSupported) {
            view.setMetadata(metadata.toString());
        } else {
            //todo show station name instead
        }
    }

    public void playPause() {
        controlInteractor.playPause();
    }

    public void skipToPrevious() {
        // TODO: 4/26/18 use control interactor
        stationsInteractor.previousStation();
    }

    public void skipToNext() {
        stationsInteractor.nextStation();
    }

    public void switchFavorite() {
        disposables.add(favoriteInteractor.switchCurrentFavorite()
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }

    public void showStation() {

    }
}
