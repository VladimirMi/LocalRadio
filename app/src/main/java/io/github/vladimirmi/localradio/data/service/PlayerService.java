package io.github.vladimirmi.localradio.data.service;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.Player;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.github.vladimirmi.localradio.utils.UiUtils;
import io.reactivex.Completable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import toothpick.Toothpick;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class PlayerService extends MediaBrowserServiceCompat implements SessionCallback.Interface {

    @Inject StationsInteractor stationsInteractor;

    private MediaSessionCompat session;
    private PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F)
            .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).build();

    private Playback playback;
    private MediaNotification notification;

    private boolean serviceStarted = false;
    private int currentStationId;
    private int playingStationId;
    private final CompositeDisposable compDisp = new CompositeDisposable();
    private final Timer stopTimer = new Timer();
    private TimerTask stopTask;


    @Override
    public void onCreate() {
        super.onCreate();
        Toothpick.inject(this, Scopes.getAppScope());

        session = new MediaSessionCompat(this, getClass().getSimpleName());
        session.setCallback(new SessionCallback(this));
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setPlaybackState(playbackState);
        setSessionToken(session.getSessionToken());

        playback = new Playback(this, playerCallback);
        notification = new MediaNotification(this, session, stationsInteractor);

        compDisp.add(stationsInteractor.getCurrentStationObs()
                .observeOn(Schedulers.io())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(null) {
                    @Override
                    public void onNext(Station station) {
                        handleCurrentStation(station);
                    }
                }));
    }

    @Override
    public void onDestroy() {
        compDisp.dispose();
        onStopCommand();
        playback.releasePlayer();
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

    private void handleCurrentStation(Station station) {
        currentStationId = station.getId();
        if (isPlayed() && currentStationId != playingStationId) playCurrent();
        notification.update();
    }

    private boolean isPlayed() {
        int state = session.getController().getPlaybackState().getState();
        return state == PlaybackStateCompat.STATE_PLAYING
                || state == PlaybackStateCompat.STATE_BUFFERING;
    }

    private boolean isPaused() {
        return session.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PAUSED;
    }


    private void playCurrent() {
        Station station = stationsInteractor.getCurrentStation();
        playingStationId = station.getId();
        playback.play(Uri.parse(station.getUrl()));
    }

    //region =============== Session callbacks ==============

    @Override
    public void onPlayCommand() {
        if (stopTask != null) stopTask.cancel();
        startService();
        if (isPaused() && currentStationId == playingStationId) {
            playback.resume();
        } else {
            playCurrent();
        }
    }

    @Override
    public void onPauseCommand(long stopDelay) {
        playback.pause();
        stopTask = new TimerTask() {
            @Override
            public void run() {
                onStopCommand();
            }
        };
        stopTimer.schedule(stopTask, stopDelay);
    }

    @Override
    public void onStopCommand() {
        playback.stop();
        session.setActive(false);
        stopSelf();
        serviceStarted = false;
    }

    @Override
    public void onSkipToPreviousCommand() {
        stationsInteractor.previousStation();
    }

    @Override
    public void onSkipToNextCommand() {
        stationsInteractor.nextStation();
    }

    //endregion

    private PlayerCallback playerCallback = new PlayerCallback() {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);
            int state;
            switch (playbackState) {
                case Player.STATE_IDLE:
                    state = PlaybackStateCompat.STATE_STOPPED;
                    break;
                case Player.STATE_BUFFERING:
                    if (playWhenReady) state = PlaybackStateCompat.STATE_BUFFERING;
                    else state = PlaybackStateCompat.STATE_PAUSED;
                    break;
                case Player.STATE_READY:
                    if (playWhenReady) state = PlaybackStateCompat.STATE_PLAYING;
                    else state = PlaybackStateCompat.STATE_PAUSED;
                    break;
                case Player.STATE_ENDED:
                    state = PlaybackStateCompat.STATE_STOPPED;
                    break;
                default:
                    state = PlaybackStateCompat.STATE_NONE;
            }
            PlaybackStateCompat newPlaybackState = new PlaybackStateCompat.Builder(PlayerService.this.playbackState)
                    .setState(state, 0, 1f)
                    .build();

            session.setPlaybackState(newPlaybackState);
            updateNotification();
        }

        @Override
        public void onMetadata(Metadata metadata) {
            super.onMetadata(metadata);
            session.setMetadata(metadata.toMediaMetadata());
            updateNotification();
        }

        @Override
        public void onPlayerError(MessageException error) {
            super.onPlayerError(error);
            onStopCommand();
            UiUtils.handleError(PlayerService.this, error);
        }
    };

    private Disposable notificationUpdate;

    private void updateNotification() {
        if (notificationUpdate != null) notificationUpdate.dispose();
        notificationUpdate = Completable.fromAction(notification::update)
                .subscribeOn(Schedulers.io())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(null));
    }
}
