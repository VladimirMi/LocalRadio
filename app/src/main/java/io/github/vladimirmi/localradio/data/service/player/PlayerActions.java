package io.github.vladimirmi.localradio.data.service.player;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;

import io.github.vladimirmi.localradio.presentation.main.MainActivity;

/**
 * Created by Vladimir Mikhalev 29.04.2018.
 */
public class PlayerActions {

    private PlayerActions() {
    }

    public static final long defaultActions = PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;

    public static PendingIntent playPauseIntent(Context context) {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_PLAY_PAUSE);
    }

    public static PendingIntent stopIntent(Context context) {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_STOP);
    }

    public static PendingIntent nextIntent(Context context) {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT);
    }

    public static PendingIntent previousIntent(Context context) {
        return MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
    }

    public static PendingIntent sessionActivity(Context context) {
        return PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
    }
}
