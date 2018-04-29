package io.github.vladimirmi.localradio.data.service;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.github.vladimirmi.localradio.data.reciever.PlayerWidget;
import io.github.vladimirmi.localradio.di.Scopes;
import io.github.vladimirmi.localradio.domain.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.MainInteractor;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.github.vladimirmi.localradio.utils.UiUtils;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import toothpick.Toothpick;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class PlayerService extends MediaBrowserServiceCompat implements SessionCallback.Interface {

    @Inject StationsInteractor stationsInteractor;
    @Inject FavoriteInteractor favoriteInteractor;
    @Inject MainInteractor mainInteractor;

    private MediaSessionCompat session;
    private PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1F)
            .setActions(PlayerActions.defaultActions).build();

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
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.e("onStartCommand: ");
        serviceStarted = true;
        session.setActive(true);
        return super.onStartCommand(intent, flags, startId);
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
            Intent startPlayerService = new Intent(this, PlayerService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startPlayerService);
            } else {
                startService(startPlayerService);
            }
        }
    }

    private void handleCurrentStation(Station station) {
        currentStationId = station.getId();
        if (isPlayed() && currentStationId != playingStationId) playCurrent();
        updateRemoteViews();
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
        Timber.e("playCurrent: " + station);
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
        serviceStarted = false;
        stopSelf();
    }

    @Override
    public void onSkipToPreviousCommand() {
        if (mainInteractor.isFavoritePage()) {
            favoriteInteractor.previousStation();
        } else {
            stationsInteractor.previousStation();
        }
    }

    @Override
    public void onSkipToNextCommand() {
        if (mainInteractor.isFavoritePage()) {
            favoriteInteractor.nextStation();
        } else {
            stationsInteractor.nextStation();
        }
    }

    //endregion

    private PlayerCallback playerCallback = new PlayerCallback() {

        @Override
        public void onPlayerStateChanged(int playbackState) {
            PlaybackStateCompat newPlaybackState = new PlaybackStateCompat.Builder(PlayerService.this.playbackState)
                    .setState(playbackState, 0, 1f)
                    .build();
            session.setPlaybackState(newPlaybackState);
            updateRemoteViews();
        }

        @Override
        public void onMetadata(Metadata metadata) {
            super.onMetadata(metadata);
            session.setMetadata(metadata.toMediaMetadata());
            updateRemoteViews();
        }

        @Override
        public void onPlayerError(MessageException error) {
            super.onPlayerError(error);
            onStopCommand();
            UiUtils.handleError(PlayerService.this, error);
        }
    };

    private void updateRemoteViews() {
        notification.update();
        PlayerWidget.update(getApplicationContext(), session);
    }
}
