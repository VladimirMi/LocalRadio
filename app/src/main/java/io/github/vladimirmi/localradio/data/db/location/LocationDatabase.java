package io.github.vladimirmi.localradio.data.db.location;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 29.06.2018.
 */
@Database(entities = {LocationEntity.class}, version = 1)
public abstract class LocationDatabase extends RoomDatabase {

    private static final String DB_NAME = "location_db";

    public abstract LocationsDao locationsDao();

    public static LocationDatabase getInstance(Context context) {
        tryCopyDatabaseFile(context);
        return Room.databaseBuilder(context.getApplicationContext(),
                LocationDatabase.class, DB_NAME).build();
    }

    private static void tryCopyDatabaseFile(Context context) {
        final File dbFile = context.getDatabasePath(DB_NAME);
        if (dbFile.exists()) return;

        try (InputStream is = context.getAssets().open(DB_NAME)) {
            try (FileOutputStream os = new FileOutputStream(dbFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
        } catch (IOException e) {
            Timber.w(e);
        }
    }
}
