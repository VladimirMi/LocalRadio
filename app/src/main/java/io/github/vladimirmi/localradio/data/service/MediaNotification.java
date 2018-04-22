package io.github.vladimirmi.localradio.data.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 22.04.2018.
 */
public class MediaNotification {

    public static final String CHANNEL_ID = "local_radio_channel";
    public static final int PLAYER_NOTIFICATION_ID = 73;

    private final PlayerService service;
    private final MediaSessionCompat session;
    private final NotificationManager notificationManager;
    private final MediaStyle mediaStyle;

    private final PendingIntent playPauseIntent;
    private final PendingIntent stopIntent;
    private final PendingIntent nextIntent;
    private final PendingIntent previousIntent;


    public MediaNotification(PlayerService service, MediaSessionCompat session) {
        this.service = service;
        this.session = session;
        notificationManager = ((NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE));

        playPauseIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        stopIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                PlaybackStateCompat.ACTION_STOP);
        nextIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
        previousIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(service,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

        mediaStyle = new MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowCancelButton(true)
                .setShowActionsInCompactView(0, 1, 2)
                .setCancelButtonIntent(stopIntent);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
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
                // TODO: 4/22/18 play with color 
                .setColor(Color.RED)
                .setStyle(mediaStyle);
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = createStandardBuilder();

        PlaybackStateCompat playbackState = session.getController().getPlaybackState();

        Metadata metadata = Metadata.create(session.getController().getMetadata());

        builder.setContentTitle(metadata.title)
                .setContentText(metadata.artist);

        if (playbackState.getState() == PlaybackStateCompat.STATE_STOPPED
                || playbackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            builder
                    .addAction(generateAction(R.drawable.ic_skip_previous, "Previous", previousIntent))
                    .addAction(generateAction(R.drawable.ic_play, "Play", playPauseIntent))
                    .addAction(generateAction(R.drawable.ic_skip_next, "Next", nextIntent));
        } else {
            builder.addAction(generateAction(R.drawable.ic_skip_previous, "Previous", previousIntent))
                    .addAction(generateAction(R.drawable.ic_stop, "Stop", playPauseIntent))
                    .addAction(generateAction(R.drawable.ic_skip_next, "Next", nextIntent));
        }
        return builder.build();
    }

    public void update() {
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
}
