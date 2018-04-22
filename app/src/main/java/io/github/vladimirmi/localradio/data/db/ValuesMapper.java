package io.github.vladimirmi.localradio.data.db;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.vladimirmi.localradio.data.db.StationContract.StationEntry;
import io.github.vladimirmi.localradio.data.entity.Station;
import io.reactivex.functions.Function;

/**
 * Created by Vladimir Mikhalev 13.04.2018.
 */
public class ValuesMapper {

    private ValuesMapper() {
    }

    public static Station cursorToStation(Cursor cursor) {
        return new Station(
                cursor.getInt(cursor.getColumnIndex(StationEntry._ID)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_CALLSIGN)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_BAND)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_GENRE)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_LANGUAGE)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_WEBSITE_URL)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_IMAGE_URL)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_ENCODING)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_STATUS)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_COUNTRY)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_CITY)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_PHONE)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_EMAIL)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_DIAL)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_SLOGAN)),
                cursor.getString(cursor.getColumnIndex(StationEntry.COLUMN_URL)),
                true
        );
    }

    public static ContentValues createValue(Station station) {
        ContentValues values = new ContentValues();
        values.put(StationEntry._ID, station.getId());
        values.put(StationEntry.COLUMN_URL, station.getUrl());
        values.put(StationEntry.COLUMN_CALLSIGN, station.getCallsign());
        values.put(StationEntry.COLUMN_DESCRIPTION, station.getDescription());
        values.put(StationEntry.COLUMN_SLOGAN, station.getSlogan());
        values.put(StationEntry.COLUMN_GENRE, station.getGenre());
        values.put(StationEntry.COLUMN_BAND, station.getBand());
        values.put(StationEntry.COLUMN_DIAL, station.getDial());
        values.put(StationEntry.COLUMN_LANGUAGE, station.getLanguage());
        values.put(StationEntry.COLUMN_COUNTRY, station.getCountryCode());
        values.put(StationEntry.COLUMN_CITY, station.getCity());
        values.put(StationEntry.COLUMN_PHONE, station.getPhone());
        values.put(StationEntry.COLUMN_EMAIL, station.getEmail());
        values.put(StationEntry.COLUMN_WEBSITE_URL, station.getWebsiteurl());
        values.put(StationEntry.COLUMN_IMAGE_URL, station.getImageurl());
        values.put(StationEntry.COLUMN_ENCODING, station.getEncoding());
        values.put(StationEntry.COLUMN_STATUS, station.getStatus());
        return values;
    }

    public static <T> List<T> getList(Cursor cursor, Function<Cursor, T> mapper) {
        if (cursor == null || cursor.getCount() == 0) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>(cursor.getCount());

        cursor.moveToFirst();
        do {
            try {
                list.add(mapper.apply(cursor));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (cursor.moveToNext());

        return list;
    }

}
