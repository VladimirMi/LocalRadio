package io.github.vladimirmi.localradio.data.service.player;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
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
    private MediaMetadataCompat mediaMetadata = Metadata.UNSUPPORTED.toMediaMetadata();

    private Playback playback;
    private MediaNotification notification;

    private boolean serviceStarted = false;
    private int currentStationId;
    private int playingStationId;
    private final CompositeDisposable compDisp = new CompositeDisposable();
    private final Timer stopTimer = new Timer();
    private TimerTask stopTask;

    private volatile boolean appInitialized;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.e("onCreate: ");
        Toothpick.inject(this, Scopes.getAppScope());

        session = new MediaSessionCompat(this, getClass().getSimpleName());
        session.setCallback(new SessionCallback(this));
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setPlaybackState(playbackState);
        session.setMetadata(mediaMetadata);
        setSessionToken(session.getSessionToken());

        playback = new Playback(this, playerCallback);
        notification = new MediaNotification(this, session);

        compDisp.add(mainInteractor.initApp()
                .doOnComplete(() -> appInitialized = true)
                .subscribeWith(new RxUtils.ErrorCompletableObserver(this)));

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
        playerCallback.onPlayerStateChanged(PlaybackStateCompat.STATE_BUFFERING);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Timber.e("onDestroy: ");
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

    private void startPlayingService() {
        if (!serviceStarted) {
            Intent startPlayerService = new Intent(this, PlayerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(startPlayerService);
            } else {
                startService(startPlayerService);
            }
            serviceStarted = true;
            session.setActive(true);
        }
    }

    private void handleCurrentStation(Station station) {
        Timber.e("handleCurrentStation: " + station.getName());
        currentStationId = station.getId();
        if (isPlayed() && currentStationId != playingStationId) playCurrent();

        Bitmap icon = UiUtils.loadBitmap(this, station.getImageUrl());

        mediaMetadata = new MediaMetadataCompat.Builder(mediaMetadata)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, station.getName())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, icon)
                .build();
        session.setMetadata(mediaMetadata);
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
        if (station.isNullStation()) return;
        playingStationId = station.getId();
        playback.play(Uri.parse(station.getUrl()));
    }

    //region =============== Session callbacks ==============

    @Override
    public void onPlayCommand() {
        Timber.e("onPlayCommand: " + serviceStarted);
        if (stopTask != null) stopTask.cancel();
        startPlayingService();
        if (isPaused() && currentStationId == playingStationId) {
            playback.resume();
        } else {
            playCurrent();
        }
    }

    private void waitAppInit() {
        while (!appInitialized) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
        waitAppInit();
        if (mainInteractor.isFavoritePage()) {
            favoriteInteractor.previousStation();
        } else {
            stationsInteractor.previousStation();
        }
    }

    @Override
    public void onSkipToNextCommand() {
        waitAppInit();
        if (mainInteractor.isFavoritePage()) {
            favoriteInteractor.nextStation();
        } else {
            stationsInteractor.nextStation();
        }
    }

    //endregion

    private PlayerCallback playerCallback = new PlayerCallback() {

        @Override
        public void onPlayerStateChanged(int state) {
            playbackState = new PlaybackStateCompat.Builder(playbackState)
                    .setState(state, 0, 1f)
                    .build();
            session.setPlaybackState(playbackState);
            updateRemoteViews();
        }

        @Override
        public void onMetadata(Metadata metadata) {
            mediaMetadata = new MediaMetadataCompat.Builder(mediaMetadata)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadata.artist)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadata.title)
                    .build();
            session.setMetadata(mediaMetadata);
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
