package io.github.vladimirmi.localradio.domain;

import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.repository.MediaController;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.data.service.Metadata;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 09.04.2018.
 */

public class PlayerControlInteractor {

    private final MediaController controller;
    private final StationsRepository stationsRepository;

    @Inject
    public PlayerControlInteractor(MediaController controller,
                                   StationsRepository stationsRepository) {
        this.controller = controller;
        this.stationsRepository = stationsRepository;
    }

    public Observable<PlaybackStateCompat> getPlaybackStateObs() {
        return controller.playbackState;
    }

    public Observable<Metadata> getPlaybackMetadataObs() {
        return controller.playbackMetadata.map(Metadata::create);
    }

    public void connect() {
        controller.connect();
    }

    public void disconnect() {
        controller.disconnect();
    }

    public void playPause() {
        if (isPlaying()) {
            controller.pause();
        } else {
            controller.play();
        }
    }

    public void stop() {
        controller.stop();
    }

    public void skipToPrevious() {
        controller.skipToPrevious();
    }

    public void skipToNext() {
        controller.skipToNext();
    }

    public boolean isPlaying() {
        return controller.playbackState.hasValue() &&
                (controller.playbackState.getValue().getState() == PlaybackStateCompat.STATE_PLAYING
                        || controller.playbackState.getValue().getState() == PlaybackStateCompat.STATE_BUFFERING);
    }

    public boolean isPaused() {
        return controller.playbackState.hasValue() &&
                (controller.playbackState.getValue().getState() == PlaybackStateCompat.STATE_PAUSED);
    }

    public boolean isStopped() {
        return controller.playbackState.hasValue() &&
                (controller.playbackState.getValue().getState() == PlaybackStateCompat.STATE_STOPPED);
    }
}
