package io.github.vladimirmi.localradio.data.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.github.vladimirmi.localradio.di.AppModule;
import io.github.vladimirmi.localradio.di.Scopes;

/**
 * Created by Vladimir Mikhalev 12.04.2018.
 */
public class StationContentProvider extends ContentProvider {

    public static final int STATIONS = 100;
    public static final int STATION_BY_ID = 101;

    private UriMatcher matcher;
    private SQLiteDatabase db;
    private ContentResolver resolver;

    @Override
    public boolean onCreate() {
        Scopes.getAppScope().installModules(new AppModule(getContext()));
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(StationContract.AUTHORITY, StationContract.PATH_STATIONS, STATIONS);
        matcher.addURI(StationContract.AUTHORITY, StationContract.PATH_STATIONS + "/#", STATION_BY_ID);

        db = Scopes.getAppScope().getInstance(SQLiteDatabase.class);
        resolver = Scopes.getAppScope().getInstance(ContentResolver.class);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        String tableName = StationContract.StationEntry.TABLE_NAME;

        switch (matcher.match(uri)) {
            case STATIONS:
                sortOrder = StationContract.StationEntry.COLUMN_TIMESTAMP + " DESC";
                break;
            case STATION_BY_ID:
                selection = StationContract.StationEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Cursor cursor = db.query(
                tableName,   // The table to query
                projection,   // The array of columns to return (pass null to get all)
                selection,   // The columns for the WHERE clause
                selectionArgs,   // The values for the WHERE clause
                null,   // don't group the rows
                null,   // don't filter by row groups
                sortOrder   // The sort order
        );
        cursor.setNotificationUri(resolver, uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri returnUri = null;

        switch (matcher.match(uri)) {
            case STATIONS:
                long id = db.insert(StationContract.StationEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(StationContract.StationEntry.CONTENT_URI, id);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (returnUri == null) throw new SQLException("Failed to insert row into " + uri);
        resolver.notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = StationContract.StationEntry.TABLE_NAME;

        switch (matcher.match(uri)) {
            case STATION_BY_ID:
                selection = StationContract.StationEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        int deleted = db.delete(tableName, selection, selectionArgs);

        if (deleted != 0) {
            resolver.notifyChange(uri, null);
        }
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String tableName = StationContract.StationEntry.TABLE_NAME;

        switch (matcher.match(uri)) {
            case STATION_BY_ID:
                selection = StationContract.StationEntry._ID + " = ?";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        int updated = db.update(tableName, values, selection, selectionArgs);

        if (updated != 0) {
            resolver.notifyChange(uri, null);
        }
        return updated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException();
    }
}
