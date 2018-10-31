package io.github.vladimirmi.localradio.data.service.player;

import android.content.SharedPreferences;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.Util;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.preferences.Preferences;

/**
 * Created by Vladimir Mikhalev 31.10.2018.
 */

public class LoadControl extends DefaultLoadControl {

    private final Preferences prefs;
    private int targetBufferSize = 0;
    private long initialBufferUs;
    private long bufferUs;
    private final long minBufferUs = DEFAULT_MIN_BUFFER_MS * 1000L;
    private final long maxBufferUs = DEFAULT_MAX_BUFFER_MS * 1000L;
    private boolean isBuffering = false;

    @Inject
    public LoadControl(Preferences preferences) {
        prefs = preferences;
        initialBufferUs = prefs.initialBufferLength.get() * 1000000L;
        bufferUs = prefs.bufferLength.get() * 1000000L;
        SharedPreferences.OnSharedPreferenceChangeListener changeListener = (sharedPreferences, key) -> {
            if (key.equals(Preferences.KEY_INITIAL_BUFFER_LENGTH)) {
                initialBufferUs = prefs.initialBufferLength.get() * 1000000L;
                reset(true);

            } else if (key.equals(Preferences.KEY_BUFFER_LENGTH)) {
                bufferUs = prefs.bufferLength.get() * 1000000L;
                reset(true);
            }
        };
        prefs.prefs.registerOnSharedPreferenceChangeListener(changeListener);
    }

    @Override
    public void onTracksSelected(Renderer[] renderers, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        targetBufferSize = calculateTargetBufferSize(renderers, trackSelections);
        ((DefaultAllocator) getAllocator()).setTargetBufferSize(targetBufferSize);
    }

    @Override
    public boolean shouldContinueLoading(long bufferedDurationUs, float playbackSpeed) {
        boolean targetBufferSizeReached = getAllocator().getTotalBytesAllocated() >= targetBufferSize;

        isBuffering = bufferedDurationUs < minBufferUs // below low watermark
                || (bufferedDurationUs <= maxBufferUs // between watermarks
                && isBuffering
                && !targetBufferSizeReached);

        return isBuffering;
    }

    @Override
    public boolean shouldStartPlayback(long bufferedDurationUs, float playbackSpeed, boolean rebuffering) {
        long bufferDuration = Util.getPlayoutDurationForMediaDuration(bufferedDurationUs, playbackSpeed);
        long minBufferDuration = rebuffering ? bufferUs : initialBufferUs;
        return minBufferDuration <= 0 || bufferDuration >= minBufferDuration;
    }

    @Override
    public void onPrepared() {
        reset(false);
    }

    @Override
    public void onStopped() {
        reset(true);
    }

    @Override
    public void onReleased() {
        reset(true);
    }

    private void reset(boolean resetAllocator) {
        targetBufferSize = 0;
        isBuffering = false;
        if (resetAllocator) {
            ((DefaultAllocator) getAllocator()).reset();
        }
    }
}
