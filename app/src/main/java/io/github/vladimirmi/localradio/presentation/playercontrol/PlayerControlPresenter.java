package io.github.vladimirmi.localradio.presentation.playercontrol;

import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.service.Metadata;
import io.github.vladimirmi.localradio.domain.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class PlayerControlPresenter extends BasePresenter<PlayerControlView> {

    private final PlayerControlInteractor controlInteractor;
    private final StationsInteractor stationsInteractor;

    @Inject
    public PlayerControlPresenter(PlayerControlInteractor controlInteractor,
                                  StationsInteractor stationsInteractor) {
        this.controlInteractor = controlInteractor;
        this.stationsInteractor = stationsInteractor;
    }

    @Override
    protected void onAttach(PlayerControlView view) {
        compDisp.add(stationsInteractor.getCurrentStationObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleCurrentStation));

        compDisp.add(controlInteractor.getPlaybackStateObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleState));

        compDisp.add(controlInteractor.getPlaybackMetadataObs()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleMetadata));
    }

    private void handleCurrentStation(Station station) {
        view.setStation(station);
    }

    private void handleState(PlaybackStateCompat state) {
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
        if (metadata.isSupported) {
            view.setMetadata(metadata.toString());
        } else {
            //todo show station callsign instead
        }
    }

    public void playPause() {
        controlInteractor.playPause();
    }

    public void skipToPrevious() {

    }

    public void skipToNext() {

    }

    public void switchFavorite() {

    }

    public void showStation() {

    }
}
