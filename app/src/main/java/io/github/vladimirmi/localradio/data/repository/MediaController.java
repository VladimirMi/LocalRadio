package io.github.vladimirmi.localradio.data.repository;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.jakewharton.rxrelay2.BehaviorRelay;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.service.PlayerService;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 09.04.2018.
 */

public class MediaController {

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    public BehaviorRelay<PlaybackStateCompat> playbackState = BehaviorRelay.create();
    public BehaviorRelay<MediaMetadataCompat> playbackMetadata = BehaviorRelay.create();

    @Inject
    public MediaController(Context context) {
        MediaBrowserCompat.ConnectionCallback connectionCallback = new MediaBrowserCompat.ConnectionCallback() {

            @Override
            public void onConnected() {
                try {
                    mediaController = new MediaControllerCompat(context, mediaBrowser.getSessionToken());
                    mediaController.registerCallback(controllerCallback);
                } catch (RemoteException e) {
                    Timber.e(e, e.getMessage());
                }
            }

            @Override
            public void onConnectionSuspended() {
                Timber.d("onConnectionSuspended");
                mediaController.unregisterCallback(controllerCallback);
                mediaController = null;
            }

            @Override
            public void onConnectionFailed() {
                Timber.d("onConnectionFailed");
            }
        };

        mediaBrowser = new MediaBrowserCompat(context,
                new ComponentName(context, PlayerService.class),
                connectionCallback, null);
    }

    private final MediaControllerCompat.Callback controllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            playbackState.accept(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            playbackMetadata.accept(metadata);
        }
    };

    public void connect() {
        if (!mediaBrowser.isConnected()) mediaBrowser.connect();
    }

    public void disconnect() {
        //todo check if need unregister callback
        if (mediaBrowser.isConnected()) mediaBrowser.disconnect();
    }

    public void play() {
        if (mediaController != null) {
            mediaController.getTransportControls().play();
        }
    }

    public void pause() {
        if (mediaController != null) {
            mediaController.getTransportControls().pause();
        }
    }

    public void stop() {
        if (mediaController != null) {
            mediaController.getTransportControls().stop();
        }
    }

    public void skipToPrevious() {
        if (mediaController != null) {
            mediaController.getTransportControls().skipToPrevious();
        }
    }

    public void skipToNext() {
        if (mediaController != null) {
            mediaController.getTransportControls().skipToNext();
        }
    }
}
