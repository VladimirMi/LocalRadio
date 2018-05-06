package io.github.vladimirmi.localradio.data.source;

import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import io.github.vladimirmi.localradio.data.service.player.Metadata;
import io.github.vladimirmi.localradio.data.service.player.PlayerCallback;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 08.04.2018.
 */

public final class IcyDataSource implements DataSource {

    private static final String TAG = "IcyDataSource";
    private static final int connectTimeoutMillis = 5000;
    private static final int readTimeoutMillis = 5000;

    private final PlayerCallback playerCallback;

    private HttpURLConnection connection;
    private InputStream inputStream;

    @SuppressWarnings("WeakerAccess")
    public IcyDataSource(PlayerCallback playerCallback) {
        this.playerCallback = playerCallback;
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        try {
            connection = makeConnection(dataSpec);
        } catch (IOException e) {
            throw new IOException("Unable to connect to " + dataSpec.uri.toString(), e);
        }

        int responseCode;
        try {
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            closeConnectionQuietly();
            throw new IOException("Unable to connect to " + dataSpec.uri.toString(), e);
        }

        // Check for a valid response code.
        if (responseCode < 200 || responseCode > 299) {
            Map<String, List<String>> headers = connection.getHeaderFields();
            closeConnectionQuietly();
            throw new IOException(String.format("Invalid response code %d: %s", responseCode, headers));
        }

        try {
            inputStream = getInputStream(connection);
        } catch (IOException e) {
            closeConnectionQuietly();
            throw e;
        }

        return dataSpec.length;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        return inputStream.read(buffer, offset, readLength);
    }

    @Override
    public Uri getUri() {
        return connection == null ? null : Uri.parse(connection.getURL().toString());
    }

    @Override
    public void close() throws IOException {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } finally {
            inputStream = null;
            closeConnectionQuietly();
        }
    }

    private InputStream getInputStream(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        int metaWindow = connection.getHeaderFieldInt("icy-metaint", 0);

        if (metaWindow > 0) {
            return new IcyInputStream(inputStream, metaWindow, playerCallback);
        } else {
            Timber.d("stream does not support icy metadata");
            playerCallback.onMetadata(Metadata.UNSUPPORTED);
            return inputStream;
        }
    }

    private HttpURLConnection makeConnection(DataSpec dataSpec) throws IOException {
        URL url = new URL(dataSpec.uri.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(connectTimeoutMillis);
        connection.setReadTimeout(readTimeoutMillis);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Icy-Metadata", "1");

        connection.connect();

        return connection;
    }

    /**
     * Closes the current connection quietly, if there is one.
     */
    private void closeConnectionQuietly() {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while disconnecting", e);
            }
            connection = null;
        }
    }
}
