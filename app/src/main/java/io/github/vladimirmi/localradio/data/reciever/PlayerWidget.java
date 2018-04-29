package io.github.vladimirmi.localradio.data.reciever;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.service.Metadata;
import io.github.vladimirmi.localradio.data.service.PlayerActions;

public class PlayerWidget extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateAppWidgets(context, createRemoteViews(context));
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
        MediaMetadataCompat metadataCompat = session.getController().getMetadata();
        Metadata metadata = Metadata.create(metadataCompat);
        PlaybackStateCompat playbackState = session.getController().getPlaybackState();

        RemoteViews views = createRemoteViews(context);

        if (metadata.isSupported) {
            views.setTextViewText(R.id.titleTv, metadata.title);
            views.setTextViewText(R.id.artistTv, metadata.artist);
        } else {
            views.setTextViewText(R.id.artistTv, context.getString(R.string.metadata_not_available));
        }

        if (playbackState.getState() == PlaybackStateCompat.STATE_STOPPED
                || playbackState.getState() == PlaybackStateCompat.STATE_PAUSED) {
            views.setInt(R.id.playPauseBt, "setBackgroundResource", R.drawable.ic_play);
        } else {
            views.setInt(R.id.playPauseBt, "setBackgroundResource", R.drawable.ic_stop);
        }

        updateAppWidgets(context, views);
    }

    private static void updateAppWidgets(Context context, RemoteViews views) {
        ComponentName appWidget = new ComponentName(context, PlayerWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(appWidget, views);
    }

    private static RemoteViews createRemoteViews(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.player_widget);

        views.setOnClickPendingIntent(R.id.previousBt, PlayerActions.previousIntent(context));
        views.setOnClickPendingIntent(R.id.playPauseBt, PlayerActions.playPauseIntent(context));
        views.setOnClickPendingIntent(R.id.nextBt, PlayerActions.nextIntent(context));

        return views;
    }
}

