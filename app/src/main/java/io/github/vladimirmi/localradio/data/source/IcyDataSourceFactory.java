package io.github.vladimirmi.localradio.data.source;

import com.google.android.exoplayer2.upstream.DataSource;

import io.github.vladimirmi.localradio.data.service.player.PlayerCallback;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class IcyDataSourceFactory implements DataSource.Factory {

    private final PlayerCallback playerCallback;

    public IcyDataSourceFactory(PlayerCallback playerCallback) {
        this.playerCallback = playerCallback;
    }

    @Override
    public DataSource createDataSource() {
        return new IcyDataSource(playerCallback);
    }
}
