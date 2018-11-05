package io.github.vladimirmi.localradio.data.service.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.support.v4.media.MediaMetadataCompat;
import androidx.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 22.04.2018.
 */
public class MediaNotification {

    private static final String CHANNEL_ID = "local_radio_channel";
    private static final int PLAYER_NOTIFICATION_ID = 73;

    private final PlayerService service;
    private final MediaSessionCompat session;
    private final NotificationManager notificationManager;
    private final MediaStyle mediaStyle;

    private final PendingIntent playPauseIntent;
    private final PendingIntent stopIntent;
    private final PendingIntent nextIntent;
    private final PendingIntent previousIntent;

    @SuppressWarnings("WeakerAccess")
    public MediaNotification(PlayerService service, MediaSessionCompat session) {
        this.service = service;
        this.session = session;

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

    public void startForeground() {
        service.startForeground(PLAYER_NOTIFICATION_ID, createNotification());
    }

    public void stopForeground(boolean removeNotification) {
        service.stopForeground(removeNotification);
//        notificationManager.cancelAll();
    }

    public void update() {
        int state = session.getController().getPlaybackState().getState();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            startForeground();

        } else if (state == PlaybackStateCompat.STATE_STOPPED) {
            stopForeground(true);

        } else {
            if (state == PlaybackStateCompat.STATE_PAUSED) {
                stopForeground(false);
            }
            notificationManager.notify(PLAYER_NOTIFICATION_ID, createNotification());
        }
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = createStandardBuilder();
        MediaMetadataCompat mediaMetadata = session.getController().getMetadata();
        int playbackState = session.getController().getPlaybackState().getState();
        Metadata metadata = Metadata.create(mediaMetadata);

        String stationName = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        Bitmap stationIcon = mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);

        builder.setSubText(stationName)
                .setLargeIcon(stationIcon);

        if (metadata.isSupported) {
            builder.setContentTitle(metadata.title)
                    .setContentText(metadata.artist);
        } else {
            builder.setContentTitle(service.getString(R.string.metadata_not_available));
        }

        if (playbackState == PlaybackStateCompat.STATE_BUFFERING) {
            builder.setContentTitle(service.getString(R.string.metadata_buffering));
        }

        builder.addAction(generateAction(R.drawable.ic_skip_previous, "Previous", previousIntent));
        if (playbackState == PlaybackStateCompat.STATE_STOPPED
                || playbackState == PlaybackStateCompat.STATE_PAUSED) {
            builder.addAction(generateAction(R.drawable.ic_play, "Play", playPauseIntent));
        } else {
            builder.addAction(generateAction(R.drawable.ic_pause, "Pause", playPauseIntent));
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
                .setSmallIcon(R.drawable.ic_headphones)
                .setContentIntent(session.getController().getSessionActivity())
                .setDeleteIntent(stopIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(mediaStyle);
    }
}
