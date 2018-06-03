package io.github.vladimirmi.localradio.data.source;

import android.support.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by Vladimir Mikhalev 03.06.2018.
 */
public class CharsetDetector {

    private static String[] CHARSETS = {
            "UTF-8",
            "Windows-1251",
            "ISO-8859-1",
            "Shift JIS",
            "Windows-1252"
    };

    @Nullable
    public static Charset detectCharset(byte[] array, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(array, offset, length);
        Charset charset = null;

        for (String charsetName : CHARSETS) {
            charset = detectCharset(buffer, Charset.forName(charsetName));
            if (charset != null) {
                break;
            }
        }
        return charset;
    }

    @Nullable
    private static Charset detectCharset(ByteBuffer buffer, Charset charset) {
        CharsetDecoder decoder = charset.newDecoder();
        decoder.reset();
        try {
            decoder.decode(buffer);
        } catch (CharacterCodingException e) {
            return null;
        }
        return charset;

    }
}
