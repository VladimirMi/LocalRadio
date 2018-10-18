package io.github.vladimirmi.localradio.data.source;

import androidx.annotation.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import io.github.vladimirmi.localradio.data.service.player.Metadata;
import io.github.vladimirmi.localradio.data.service.player.PlayerCallback;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class IcyInputStream extends FilterInputStream {

    private final int window;
    private final PlayerCallback playerCallback;

    private int bytesBeforeMetadata;
    private byte[] buffer = new byte[128];

    @SuppressWarnings("WeakerAccess")
    public IcyInputStream(InputStream in, int window, PlayerCallback playerCallback) {
        super(in);
        this.window = window;
        this.bytesBeforeMetadata = window;
        this.playerCallback = playerCallback;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        if (--bytesBeforeMetadata == 0) readMetadata();
        return read;
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, bytesBeforeMetadata < len ? bytesBeforeMetadata : len);
        bytesBeforeMetadata -= read;
        if (bytesBeforeMetadata == 0) readMetadata();
        return read;
    }

    private void readMetadata() throws IOException {
        bytesBeforeMetadata = window;
        int size = super.read() * 16;
        if (size < 1) return;
        if (size > buffer.length) {
            buffer = new byte[size];
        }
        ensureFill(buffer, 0, size);
        int actualSize = 0;
        for (int i = 0; i < size; i++) {
            if (buffer[i] == 0) {
                actualSize = i;
                break;
            }
        }
        Charset charset = CharsetDetector.detectCharset(buffer, 0, actualSize);

        String meta = new String(buffer, 0, actualSize,
                charset == null ? Charset.defaultCharset() : charset);

        playerCallback.onMetadata(Metadata.create(meta));
    }


    private int ensureFill(byte[] buffer, int offset, int size) throws IOException {
        int read = super.read(buffer, offset, size);

        if (read != -1 && size - read > 0) {
            return read + ensureFill(buffer, offset + read, size - read);
        } else {
            return read;
        }
    }
}
