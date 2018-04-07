package io.github.vladimirmi.localradio.data.service;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Collections;
import java.util.List;

import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class PlayerService extends MediaBrowserServiceCompat implements SessionCallback.Interface {

    private MediaSessionCompat session;
    private PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F)
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build();

    private Playback playback;

    private boolean serviceStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        session = new MediaSessionCompat(this, getClass().getSimpleName());
        session.setCallback(new SessionCallback(this));
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setPlaybackState(playbackState);
        setSessionToken(session.getSessionToken());

        playback = new Playback(this, playerCallback);
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(Collections.emptyList());
    }

    private void startService() {
        if (!serviceStarted) {
            startService(new Intent(getApplicationContext(), PlayerService.class));
            serviceStarted = true;
            session.setActive(true);
        }
    }

    //region =============== Session callbacks ==============

    @Override
    public void onPlayCommand() {
        startService();
    }

    @Override
    public void onPauseCommand(long stopDelay) {

    }

    @Override
    public void onStopCommand() {

    }

    @Override
    public void onSkipToPreviousCommand() {

    }

    @Override
    public void onSkipToNextCommand() {

    }

    //endregion

    private PlayerCallback playerCallback = new PlayerCallback() {


    };
}
