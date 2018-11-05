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

public abstract class PlayerCallback implements Player.EventListener {

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

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        MessageException exception;
        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                Timber.w(error.getSourceException(), "SOURCE error occurred");
                exception = new MessageException(R.string.error_connection);
                break;
            case ExoPlaybackException.TYPE_RENDERER:
                Timber.w(error.getRendererException(), "RENDERER error occurred");
                exception = new MessageException(R.string.error_renderer);
                break;
            default:
                Timber.w(error.getUnexpectedException(), "UNEXPECTED error occurred");
                exception = new MessageException(R.string.error_unexpected);
                break;
        }
        onPlayerError(exception);
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

    public abstract void onPlayerStateChanged(int playbackState);

    public abstract void onMetadata(Metadata metadata);

    public abstract void onPlayerError(MessageException error);
}
