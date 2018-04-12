package io.github.vladimirmi.localradio.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.vladimirmi.localradio.data.db.StationContract.StationEntry;

/**
 * Created by Vladimir Mikhalev 12.04.2018.
 */
public final class StationsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "stations.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_STATIONS_TABLE = "CREATE TABLE " +
            StationEntry.TABLE_NAME + " (" +
            StationEntry._ID + " INTEGER PRIMARY KEY," +
            StationEntry.COLUMN_URL + " TEXT NOT NULL," +
            StationEntry.COLUMN_CALLSIGN + " TEXT NOT NULL," +
            StationEntry.COLUMN_DESCRIPTION + " TEXT," +
            StationEntry.COLUMN_SLOGAN + " TEXT," +
            StationEntry.COLUMN_GENRE + " TEXT NOT NULL," +
            StationEntry.COLUMN_BAND + " TEXT NOT NULL," +
            StationEntry.COLUMN_DIAL + " REAL," +
            StationEntry.COLUMN_LANGUAGE + " TEXT NOT NULL," +
            StationEntry.COLUMN_COUNTRY + " TEXT NOT NULL," +
            StationEntry.COLUMN_CITY + " TEXT," +
            StationEntry.COLUMN_PHONE + " TEXT," +
            StationEntry.COLUMN_EMAIL + " TEXT," +
            StationEntry.COLUMN_WEBSITEURL + " TEXT," +
            StationEntry.COLUMN_IMAGEURL + " TEXT," +
            StationEntry.COLUMN_ENCODING + " TEXT," +
            StationEntry.COLUMN_STATUS + " TEXT," +
            StationEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    public StationsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_STATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StationEntry.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
