package io.github.vladimirmi.localradio.data.service.player;

import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;

import io.github.vladimirmi.localradio.BuildConfig;
import io.github.vladimirmi.localradio.data.source.IcyDataSourceFactory;
import io.github.vladimirmi.localradio.di.Scopes;

import static android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class Playback implements AudioManager.OnAudioFocusChangeListener {

    public static final int STOP_DELAY = 60000; // default stop delay 1 min
    private static final int STOP_DELAY_HEADSET = 3 * 60000; // stop delay on headset unplug
    private static final float VOLUME_DUCK = 0.2f;
    private static final float VOLUME_NORMAL = 1.0f;

    private final PlayerService service;
    private final PlayerCallback callback;
    private final AudioManager audioManager;
    private final WifiManager.WifiLock wifiLock;
    private final LoadControl loadControl;

    private boolean playAgainOnFocus = false;
    private boolean playAgainOnHeadset = false;
    private SimpleExoPlayer player = null;

    @SuppressWarnings("WeakerAccess")
    public Playback(PlayerService service, PlayerCallback callback) {
        this.service = service;
        this.callback = callback;

        audioManager = ((AudioManager) service.getSystemService(Context.AUDIO_SERVICE));
        //noinspection ConstantConditions
        wifiLock = ((WifiManager) service.getApplicationContext().getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, BuildConfig.APPLICATION_ID);
        loadControl = Scopes.getAppScope().getInstance(LoadControl.class);
    }

    public void play(Uri uri) {
        if (player == null) createPlayer();
        preparePlayer(uri);
        if (holdResources()) resume();
    }

    public void resume() {
        player.setPlayWhenReady(true);
    }

    public void pause() {
        player.setPlayWhenReady(false);
    }

    public void stop() {
        playAgainOnFocus = false;
        playAgainOnHeadset = false;
        releaseResources();
        if (player != null) {
            player.stop();
        }
    }

    public void releasePlayer() {
        if (player != null) {
            player.removeListener(callback);
            player.release();
            player = null;
        }
    }

    @Override
    public void onAudioFocusChange(int focus) {
        if (focus == AudioManager.AUDIOFOCUS_GAIN) {
            player.setVolume(VOLUME_NORMAL);
            if (playAgainOnFocus) {
                resume();
            }
        } else if (focus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            player.setVolume(VOLUME_DUCK);

        } else if (focus == AudioManager.AUDIOFOCUS_LOSS || focus == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            playAgainOnFocus = player.getPlayWhenReady();
            pause();

        } else {
            pause();
        }
    }

    private void createPlayer() {
        RenderersFactory renderersFactory = new DefaultRenderersFactory(service);
        TrackSelector selector = new DefaultTrackSelector();

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, selector, loadControl);
        player.addListener(callback);
    }

    private void preparePlayer(Uri uri) {
        IcyDataSourceFactory dataSourceFactory = new IcyDataSourceFactory(callback);
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);

        player.prepare(mediaSource);
    }

    private boolean holdResources() {
        registerAudioNoisyReceiver();
        if (!wifiLock.isHeld()) wifiLock.acquire();
        return audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private void releaseResources() {
        audioManager.abandonAudioFocus(this);
        unregisterAudioNoisyReceiver();
        if (wifiLock.isHeld()) wifiLock.release();
    }

    private BroadcastReceiver audioNoisyReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                playAgainOnHeadset = player != null && player.getPlayWhenReady();
                service.onPauseCommand(STOP_DELAY_HEADSET);

            } else if (playAgainOnHeadset && Intent.ACTION_HEADSET_PLUG.equals(action)
                    && intent.getIntExtra("state", 0) == 1) {
                service.onPlayCommand();

            } else if (playAgainOnHeadset && BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)
                    && intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, 0) == BluetoothHeadset.STATE_CONNECTED) {
                service.onPlayCommand();
            }
        }
    };

    private volatile boolean audioNoisyReceiverRegistered = false;

    private void registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ACTION_AUDIO_BECOMING_NOISY);
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
            service.registerReceiver(audioNoisyReceiver, filter);
            audioNoisyReceiverRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (audioNoisyReceiverRegistered) {
            service.unregisterReceiver(audioNoisyReceiver);
            audioNoisyReceiverRegistered = false;
        }
    }
}
