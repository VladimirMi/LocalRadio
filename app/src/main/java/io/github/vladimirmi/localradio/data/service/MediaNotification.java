package io.github.vladimirmi.localradio.data.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.RequiresApi;
import android.support.annotation.WorkerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;

import java.util.concurrent.TimeUnit;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.StationsInteractor;
import io.github.vladimirmi.localradio.utils.RxUtils;
import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 22.04.2018.
 */
public class MediaNotification {

    private static final String CHANNEL_ID = "local_radio_channel";
    private static final int PLAYER_NOTIFICATION_ID = 73;

    private final PlayerService service;
    private final MediaSessionCompat session;
    private final StationsInteractor stationsInteractor;
    private final NotificationManager notificationManager;
    private final MediaStyle mediaStyle;

    private final PendingIntent playPauseIntent;
    private final PendingIntent stopIntent;
    private final PendingIntent nextIntent;
    private final PendingIntent previousIntent;

    private Disposable notificationUpdate;

    public MediaNotification(PlayerService service, MediaSessionCompat session, StationsInteractor stationsInteractor) {
        this.service = service;
        this.session = session;
        this.stationsInteractor = stationsInteractor;

        notificationManager = ((NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE));

        playPauseIntent = PlayerActions.playPauseIntent(service.getApplicationContext());
        stopIntent = PlayerActions.stopIntent(service.getApplicationContext());
        nextIntent = PlayerActions.nextIntent(service.getApplicationContext());
        previousIntent = PlayerActions.previousIntent(service.getApplicationContext());

        mediaStyle = new MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowCancelButton(true)
                .setShowActionsInCompactView(0, 1, 2)
                .setCancelButtonIntent(stopIntent);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    @MainThread
    public void update() {
        if (notificationUpdate != null) notificationUpdate.dispose();
        notificationUpdate = Completable.fromAction(this::updateInBackground)
                .subscribeOn(Schedulers.io())
                .subscribeWith(new RxUtils.ErrorCompletableObserver(null));
    }

    @WorkerThread
    private void updateInBackground() {
        int state = session.getController().getPlaybackState().getState();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            service.startForeground(PLAYER_NOTIFICATION_ID, createNotification());

        } else if (state == PlaybackStateCompat.STATE_STOPPED) {
            service.stopForeground(true);
            notificationManager.cancelAll();

        } else {
            if (state == PlaybackStateCompat.STATE_PAUSED) {
                service.stopForeground(false);
            }
            notificationManager.notify(PLAYER_NOTIFICATION_ID, createNotification());
        }
    }

    @WorkerThread
    private Notification createNotification() {
        NotificationCompat.Builder builder = createStandardBuilder();
        PlaybackStateCompat playbackState = session.getController().getPlaybackState();
        // TODO: 4/29/18 extract bitmap
        Metadata metadata = Metadata.create(session.getController().getMetadata());

        builder.setSubText(stationsInteractor.getCurrentStation().getName());

        if (metadata.isSupported) {
            builder.setContentTitle(metadata.title)
                    .setContentText(metadata.artist);
        } else {
            builder.setContentText(service.getString(R.string.metadata_not_available));
        }

        Resources resources = service.getResources();
        int width = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int height = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

        FutureTarget<Bitmap> futureTarget = Glide.with(service.getApplicationContext())
                .load(stationsInteractor.getCurrentStation().getImageUrl())
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .error(R.drawable.ic_radio)
                .into(width, height);

        try {
            builder.setLargeIcon(futureTarget.get(3000, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            Timber.w("error loading notification icon");
        } finally {
            Glide.clear(futureTarget);
        }


        builder.addAction(generateAction(R.drawable.ic_skip_previous, "Previous", previousIntent));
        if (playbackState.getState() == PlaybackStateCompat.STATE_STOPPED
                || playbackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            builder.addAction(generateAction(R.drawable.ic_play, "Play", playPauseIntent));
        } else {
            builder.addAction(generateAction(R.drawable.ic_stop, "Stop", playPauseIntent));
        }
        builder.addAction(generateAction(R.drawable.ic_skip_next, "Next", nextIntent));

        return builder.build();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String channelName = service.getString(R.string.notification_name);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName,
                NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    private NotificationCompat.Action generateAction(int icon, String title, PendingIntent action) {
        return new NotificationCompat.Action.Builder(icon, title, action).build();
    }

    private NotificationCompat.Builder createStandardBuilder() {
        return new NotificationCompat.Builder(service, CHANNEL_ID)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_radio)
                .setContentIntent(session.getController().getSessionActivity())
                .setDeleteIntent(stopIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(mediaStyle);
    }

}
