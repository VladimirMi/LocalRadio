package io.github.vladimirmi.localradio.data.service;

import android.support.v4.media.session.MediaSessionCompat;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class SessionCallback extends MediaSessionCompat.Callback {

    private final Interface callback;

    public SessionCallback(Interface callback) {
        super();
        this.callback = callback;
    }

    @Override
    public void onPlay() {
        callback.onPlayCommand();
    }

    @Override
    public void onPause() {
        callback.onPauseCommand();
    }

    @Override
    public void onStop() {
        callback.onStopCommand();
    }

    @Override
    public void onSkipToPrevious() {
        callback.onSkipToPreviousCommand();
    }

    @Override
    public void onSkipToNext() {
        callback.onSkipToNextCommand();
    }

    interface Interface {

        void onPlayCommand();

        void onPauseCommand();

        void onStopCommand();

        void onSkipToPreviousCommand();

        void onSkipToNextCommand();
    }
}
