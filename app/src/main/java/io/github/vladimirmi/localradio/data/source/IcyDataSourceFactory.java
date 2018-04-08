package io.github.vladimirmi.localradio.data.source;

import com.google.android.exoplayer2.upstream.DataSource;

import io.github.vladimirmi.localradio.data.service.PlayerCallback;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class IcyDataSourceFactory implements DataSource.Factory {

    private final String userAgent;
    private final PlayerCallback playerCallback;

    public IcyDataSourceFactory(String userAgent, PlayerCallback playerCallback) {
        this.userAgent = userAgent;
        this.playerCallback = playerCallback;
    }

    @Override
    public DataSource createDataSource() {
        return new IcyDataSource(userAgent, null, playerCallback);
    }
}
