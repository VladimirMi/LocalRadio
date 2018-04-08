package io.github.vladimirmi.localradio.data.source;

import com.google.android.exoplayer2.util.Predicate;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import io.github.vladimirmi.localradio.com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import io.github.vladimirmi.localradio.data.service.PlayerCallback;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public class IcyDataSource extends DefaultHttpDataSource {

    private final PlayerCallback playerCallback;

    public IcyDataSource(String userAgent,
                         Predicate<String> contentTypePredicate,
                         PlayerCallback playerCallback) {
        super(userAgent, contentTypePredicate);

        this.playerCallback = playerCallback;

        setRequestProperty("Icy-Metadata", "1");
    }

    @Override
    protected InputStream getInputStream(HttpURLConnection connection) throws IOException {
        int metaWindow = connection.getHeaderFieldInt("icy-metaint", 0);

        if (metaWindow > 0) {
            return new IcyInputStream(connection.getInputStream(), metaWindow, playerCallback);
        } else {
            Timber.d("stream does not support icy metadata");
            return super.getInputStream(connection);
        }
    }
}
