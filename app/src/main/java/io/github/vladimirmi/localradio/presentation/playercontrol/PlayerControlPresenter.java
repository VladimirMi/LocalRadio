package io.github.vladimirmi.localradio.presentation.playercontrol;

import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.service.player.Metadata;
import io.github.vladimirmi.localradio.domain.interactors.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.interactors.MainInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class PlayerControlPresenter extends BasePresenter<PlayerControlView> {

    private final PlayerControlInteractor controlInteractor;
    private final StationsInteractor stationsInteractor;
    private final FavoriteInteractor favoriteInteractor;
    private final MainInteractor mainInteractor;


    @SuppressWarnings("WeakerAccess")
    @Inject
    public PlayerControlPresenter(PlayerControlInteractor controlInteractor,
                                  StationsInteractor stationsInteractor,
                                  FavoriteInteractor favoriteInteractor,
                                  MainInteractor mainInteractor) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
        this.favoriteInteractor = favoriteInteractor;
        this.mainInteractor = mainInteractor;
    }

    @Override
    protected void onAttach(PlayerControlView view, boolean isFirstAttach) {
        disposables.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(view) {
                    @Override
                    public void onNext(Station station) {
                        view.setStation(station);
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

        disposables.add(favoriteInteractor.isCurrentStationFavorite()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new RxUtils.ErrorObserver<Boolean>(view) {
                    @Override
                    public void onNext(Boolean isFavorite) {
                        Timber.e("onNext: " + isFavorite);
                        view.setFavorite(isFavorite);
                    }
                }));
    }

    private void handleState(PlaybackStateCompat state) {
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                view.showPlaying();
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                view.showLoading();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                view.showStopped();
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                view.showStopped();
                break;
        }
    }

    private void handleMetadata(Metadata metadata) {
        if (metadata.isSupported && !metadata.isEmpty) {
            view.setMetadata(metadata.toString());
        } else if (stationsInteractor.getCurrentStation() != null) {
            view.setMetadata(stationsInteractor.getCurrentStation().name);
        }
    }

    public void playPause() {
        controlInteractor.playPause();
    }

    public void skipToPrevious() {
        if (mainInteractor.isFavoritePage()) {
            favoriteInteractor.previousStation();
        } else {
            stationsInteractor.previousStation();
        }
    }

    public void skipToNext() {
        if (mainInteractor.isFavoritePage()) {
            favoriteInteractor.nextStation();
        } else {
            stationsInteractor.nextStation();
        }
    }

    public void switchFavorite() {
        disposables.add(favoriteInteractor.switchCurrentFavorite()
                .subscribeWith(new RxUtils.ErrorCompletableObserver(view)));
    }
}
