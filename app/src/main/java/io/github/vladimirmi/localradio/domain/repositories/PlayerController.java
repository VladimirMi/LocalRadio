package io.github.vladimirmi.localradio.domain.repositories;

import android.support.v4.media.session.PlaybackStateCompat;

import io.github.vladimirmi.localradio.data.service.player.Metadata;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 28.05.2018.
 */
public interface PlayerController {

    Observable<PlaybackStateCompat> getState();

    Observable<Metadata> getMetadata();

    void connect();

    void disconnect();

    void play();

    void pause();

    void stop();

    void skipToPrevious();

    void skipToNext();

    boolean isPlaying();

    boolean isPaused();

    boolean isStopped();
}
