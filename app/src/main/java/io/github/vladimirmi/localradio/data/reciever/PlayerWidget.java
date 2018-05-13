package io.github.vladimirmi.localradio.data.reciever;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.RemoteViews;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.service.player.Metadata;
import io.github.vladimirmi.localradio.data.service.player.PlayerActions;
import io.github.vladimirmi.localradio.data.service.player.PlayerService;
import io.github.vladimirmi.localradio.presentation.main.MainActivity;

public class PlayerWidget extends AppWidgetProvider {

    public static final int REQUEST_CODE_WIDGET = 200;
    public static final String ACTION_WIDGET_UPDATE = "ACTION_WIDGET_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = new Intent(context, PlayerService.class);
        intent.setAction(ACTION_WIDGET_UPDATE);
        startForegroundService(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void update(Context context, MediaSessionCompat session) {
        RemoteViews views = createRemoteViews(context);

        PlaybackStateCompat playbackState = session.getController().getPlaybackState();
        MediaMetadataCompat mediaMetadata = session.getController().getMetadata();
        Metadata metadata = Metadata.create(mediaMetadata);

        String stationName = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        Bitmap stationIcon = mediaMetadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);

        views.setTextViewText(R.id.stationNameTv, stationName);
        views.setImageViewBitmap(R.id.iconIv, stationIcon);

        if (metadata.isSupported) {
            views.setTextViewText(R.id.metadataTv, metadata.toString());
        } else {
            views.setTextViewText(R.id.metadataTv, context.getString(R.string.metadata_not_available));
        }

        if (playbackState.getState() == PlaybackStateCompat.STATE_BUFFERING) {
            views.setViewVisibility(R.id.loadingPb, View.VISIBLE);

        } else if (playbackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            views.setViewVisibility(R.id.loadingPb, View.GONE);
        }

        if (playbackState.getState() == PlaybackStateCompat.STATE_STOPPED
                || playbackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            views.setInt(R.id.playPauseBt, "setBackgroundResource", R.drawable.ic_play);
            views.setViewVisibility(R.id.loadingPb, View.GONE);

        } else {
            views.setInt(R.id.playPauseBt, "setBackgroundResource", R.drawable.ic_pause);
        }

        updateAppWidgets(context, views);
    }

    private static RemoteViews createRemoteViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.player_widget);

        views.setOnClickPendingIntent(R.id.previousBt, PlayerActions.previousIntent(context));
        views.setOnClickPendingIntent(R.id.playPauseBt, PlayerActions.playPauseIntent(context));
        views.setOnClickPendingIntent(R.id.nextBt, PlayerActions.nextIntent(context));

        Intent startApp = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, REQUEST_CODE_WIDGET, startApp, 0);
        views.setOnClickPendingIntent(R.id.iconIv, pendingIntent);

        return views;
    }

    private static void updateAppWidgets(Context context, RemoteViews views) {
        ComponentName appWidget = new ComponentName(context, PlayerWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidget, views);
    }

    private static void startForegroundService(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
}

