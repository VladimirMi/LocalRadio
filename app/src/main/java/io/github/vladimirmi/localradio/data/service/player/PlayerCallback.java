package io.github.vladimirmi.localradio.data.service.player;

import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.utils.MessageException;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class PlayerCallback implements Player.EventListener {

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
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
        onPlayerStateChanged(state);
    }

    public void onPlayerStateChanged(int playbackState) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                Timber.w("SOURCE error occurred: %s", error.getSourceException());
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                Timber.w("RENDERER error occurred: %s", error.getRendererException());
                break;
            case ExoPlaybackException.TYPE_UNEXPECTED:
                Timber.w("UNEXPECTED error occurred: %s", error.getUnexpectedException());
                break;
        }
        onPlayerError(transformToMessage(error));
    }

    public void onPlayerError(MessageException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    public void onMetadata(Metadata metadata) {

    }

    private MessageException transformToMessage(ExoPlaybackException error) {
        MessageException exception;
        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                exception = new MessageException(R.string.error_connection);
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                exception = new MessageException(R.string.error_renderer);
                break;
            default:
                exception = new MessageException(R.string.error_unexpected);
                break;
        }
        return exception;
    }
}
