package io.github.vladimirmi.localradio.domain.interactors;

import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.service.player.Metadata;
import io.github.vladimirmi.localradio.domain.repositories.PlayerController;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 09.04.2018.
 */

public class PlayerControlsInteractor {

    private final PlayerController controller;

    @Inject
    public PlayerControlsInteractor(PlayerController controller) {
        this.controller = controller;
    }

    // TODO: 5/28/18 remove PlaybackStateCompat dependency
    public Observable<PlaybackStateCompat> getPlaybackStateObs() {
        return controller.getState();
    }

    public Observable<Metadata> getPlaybackMetadataObs() {
        return controller.getMetadata();
    }

    public void connect() {
        controller.connect();
    }

    public void disconnect() {
        controller.disconnect();
    }

    public void playPause() {
        if (controller.isPlaying()) {
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
}
