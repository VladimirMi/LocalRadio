package io.github.vladimirmi.localradio.data.service.player;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
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
        Toothpick.inject(this, Scopes.getAppScope());

        initSession();
        playback = new Playback(this, playerCallback);
        notification = new MediaNotification(this, session);

        compDisp.add(stationsInteractor.getCurrentStationObs()
                .observeOn(Schedulers.io())
                .distinctUntilChanged(Station::getId)
                .filter(station -> !station.isNullStation())
                .subscribeWith(new RxUtils.ErrorObserver<Station>(null) {
                    @Override
                    public void onNext(Station station) {
                        appInitialized = true;
                        handleCurrentStation(station);
                    }
                }));
    }

    private void initSession() {
        session = new MediaSessionCompat(this, getClass().getSimpleName());
        session.setCallback(new SessionCallback(this));
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setPlaybackState(playbackState);
        session.setMetadata(mediaMetadata);
        setSessionToken(session.getSessionToken());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        notification.startForeground();
        serviceStarted = true;
        session.setActive(true);

        if (isPaused()) scheduleStopTask(SessionCallback.STOP_DELAY);

        if (!appInitialized) {
            compDisp.add(mainInteractor.initApp()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnComplete(() -> {
                        appInitialized = true;
                        MediaButtonReceiver.handleIntent(session, intent);
                    })
                    .doOnError(e -> notification.stopForeground(true))
                    .subscribeWith(new RxUtils.ErrorCompletableObserver(this)));
        } else {
            MediaButtonReceiver.handleIntent(session, intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        compDisp.dispose();
        serviceStarted = false;
        session.setActive(false);
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
            startService(startPlayerService);
        }
    }

    @WorkerThread
    private void handleCurrentStation(Station station) {
        currentStationId = station.getId();
        if (isPlayed() && currentStationId != playingStationId) playCurrent();

        Bitmap icon = UiUtils.loadBitmapForStation(this, station);

        clearMetadata();
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
        scheduleStopTask(stopDelay);
    }

    @Override
    public void onStopCommand() {
        playback.stop();
        stopSelf();
    }

    @Override
    public void onSkipToPreviousCommand() {
        if (stationsInteractor.getCurrentStation().isFavorite()) {
            favoriteInteractor.previousStation();
        } else {
            stationsInteractor.previousStation();
        }
    }

    @Override
    public void onSkipToNextCommand() {
        if (stationsInteractor.getCurrentStation().isFavorite()) {
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
            if (state == PlaybackStateCompat.STATE_STOPPED) {
                clearMetadata();
                session.setMetadata(mediaMetadata);
            }
            updateRemoteViews();
        }

        @Override
        public void onMetadata(Metadata metadata) {
            updateMetadata(metadata.artist, metadata.title);
            session.setMetadata(mediaMetadata);
            updateRemoteViews();
        }

        @Override
        public void onPlayerError(MessageException error) {
            onStopCommand();
            UiUtils.handleError(PlayerService.this, error);
        }
    };

    private void clearMetadata() {
        updateMetadata("", "");
    }

    private void updateMetadata(String artist, String title) {
        mediaMetadata = new MediaMetadataCompat.Builder(mediaMetadata)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build();
    }

    private void updateRemoteViews() {
        notification.update();
        PlayerWidget.update(getApplicationContext(), session);
    }

    private void scheduleStopTask(long stopDelay) {
        if (stopTask != null) stopTask.cancel();
        stopTask = new TimerTask() {
            @Override
            public void run() {
                onStopCommand();
            }
        };
        stopTimer.schedule(stopTask, stopDelay);
    }
}
