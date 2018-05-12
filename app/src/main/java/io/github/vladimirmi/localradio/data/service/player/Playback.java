package io.github.vladimirmi.localradio.data.service.player;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultAllocator;

import io.github.vladimirmi.localradio.BuildConfig;
import io.github.vladimirmi.localradio.data.source.IcyDataSourceFactory;

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
    }

    public void play(Uri uri) {
        holdResources();
        if (player == null) createPlayer();
        preparePlayer(uri);
        resume();
    }

    public void resume() {
        playAgainOnFocus = true;
        player.setPlayWhenReady(true);
    }

    public void pause() {
        playAgainOnFocus = false;
        playAgainOnHeadset = false;
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
        LoadControl loadControl = new DefaultLoadControl(
                new DefaultAllocator(true, C.DEFAULT_AUDIO_BUFFER_SIZE));

        player = ExoPlayerFactory.newSimpleInstance(renderersFactory, selector, loadControl);
        player.addListener(callback);
    }

    private void preparePlayer(Uri uri) {
        IcyDataSourceFactory dataSourceFactory = new IcyDataSourceFactory(callback);
        ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);

        player.prepare(mediaSource);
    }

    private void holdResources() {
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        registerAudioNoisyReceiver();
        if (!wifiLock.isHeld()) wifiLock.acquire();
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
                service.onPauseCommand(STOP_DELAY_HEADSET);
                playAgainOnHeadset = player != null && player.getPlayWhenReady();

            } else if (playAgainOnHeadset && Intent.ACTION_HEADSET_PLUG.equals(action)
                    && intent.getIntExtra("state", 0) == 1) {
                service.onPlayCommand();

            } else if (playAgainOnHeadset && BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                int count = 0;
                while (!audioManager.isBluetoothA2dpOn() && count < 10) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                }
                if (audioManager.isBluetoothA2dpOn()) {
                    service.onPlayCommand();
                }
            }
        }
    };

    private volatile boolean audioNoisyReceiverRegistered = false;

    private void registerAudioNoisyReceiver() {
        if (!audioNoisyReceiverRegistered) {
            IntentFilter filter = new IntentFilter(ACTION_AUDIO_BECOMING_NOISY);
            filter.addAction(Intent.ACTION_HEADSET_PLUG);
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
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
