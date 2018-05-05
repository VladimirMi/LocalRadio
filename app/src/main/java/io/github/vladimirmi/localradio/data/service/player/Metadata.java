package io.github.vladimirmi.localradio.data.service.player;

import android.support.v4.media.MediaMetadataCompat;

import io.github.vladimirmi.localradio.utils.StringUtils;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 07.04.2018.
 */

public class Metadata {

    private static final String unsupported = "unsupported";

    public final String artist;
    public final String title;
    public final boolean isSupported;

    private Metadata(String artist, String title) {
        this.artist = artist;
        this.title = title;
        isSupported = !artist.equals(unsupported) && !title.equals(unsupported);
    }

    public static Metadata UNSUPPORTED = new Metadata(unsupported, unsupported);

    public static Metadata create(String meta) {
        Timber.e("create: " + meta);
        String artistTitle = new StringUtils.Builder(meta).substringAfter("StreamTitle=", unsupported)
                .substringBefore(";")
                .trim(' ', '\'')
                .toString();

        String[] strings = splitOnArtistTitle(artistTitle);

        String artist = strings[0];
        String title = strings[1];

        if (title.isEmpty()) return UNSUPPORTED;
        if (title.endsWith("]")) title = StringUtils.substringBeforeLast(title, "[", title);
        if (title.startsWith("text=")) {
            title = new StringUtils.Builder(title)
                    .substringAfter("text=\"")
                    .substringBefore("\"")
                    .toString();
        }

        return new Metadata(artist.trim(), title.trim());
    }

    public static Metadata create(MediaMetadataCompat meta) {
        if (meta == null) return UNSUPPORTED;
        String subtitle = meta.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        String title = meta.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        return new Metadata(
                subtitle.isEmpty() ? unsupported : subtitle,
                title.isEmpty() ? unsupported : title
        );
    }

    public MediaMetadataCompat toMediaMetadata() {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build();
    }

    public String toLogString() {
        if (!isSupported) {
            return "Metadata(Unsupported)";
        } else {
            return String.format("Metadata(artist='%s', title='%s')", artist, title);
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s", artist, title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Metadata metadata = (Metadata) o;

        return artist.equals(metadata.artist) && title.equals(metadata.title);
    }

    @Override
    public int hashCode() {
        int result = artist.hashCode();
        result = 31 * result + title.hashCode();
        return result;
    }

    private static String[] splitOnArtistTitle(String artistTitle) {
        String[] strings;
        int i = artistTitle.indexOf('-');
        int j = artistTitle.indexOf(':');

        if (i == -1 && j == -1) {
            strings = new String[]{"", artistTitle};
        } else if (i == -1) {
            strings = artistTitle.split(":", 2);
        } else if (j == -1) {
            strings = artistTitle.split(" - ", 2);
        } else if (i < j) {
            strings = artistTitle.split(" - ", 2);
        } else {
            strings = artistTitle.split(":", 2);
        }
        if (strings.length == 1) {
            strings = new String[]{"", strings[0]};
        }
        return strings;
    }
}
