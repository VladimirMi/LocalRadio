package io.github.vladimirmi.localradio.data.db;

import android.net.Uri;
import android.provider.BaseColumns;

import io.github.vladimirmi.localradio.BuildConfig;

/**
 * Created by Vladimir Mikhalev 12.04.2018.
 */
public final class StationContract {

    private StationContract() {
    }

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_STATIONS = "stations";


    public static final class StationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_STATIONS).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_SLOGAN = "slogan";
        public static final String COLUMN_GENRE = "genre";
        public static final String COLUMN_BAND = "band";
        public static final String COLUMN_DIAL = "dial";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_COUNTRY = "country";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_WEBSITE_URL = "websiteUrl";
        public static final String COLUMN_IMAGE_URL = "imageUrl";
        public static final String COLUMN_ENCODING = "encoding";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_TIMESTAMP = "createdAt";
    }
}
