package io.github.vladimirmi.localradio.data.db.location;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 29.06.2018.
 */
@Database(entities = {LocationEntity.class}, version = 1, exportSchema = false)
public abstract class LocationDatabase extends RoomDatabase {

    private static final String DB_NAME = "locations.db";

    public abstract LocationsDao locationsDao();

    public static LocationDatabase getInstance(Context context) {
        tryCopyDatabaseFile(context);
        return Room.databaseBuilder(context.getApplicationContext(),
                LocationDatabase.class, DB_NAME)
                .build();
    }

    private static void tryCopyDatabaseFile(Context context) {
        try {
            String[] assetsNames = context.getAssets().list("");
            if (assetsNames == null) return;
            for (String name : assetsNames) {
                String[] nameExtension = name.split("\\.");
                if (nameExtension.length == 2 && nameExtension[1].equals("ver")) {
                    Integer assetsVersion = Integer.valueOf(nameExtension[0]);

                    Preferences preferences = new Preferences(context);
                    Integer version = preferences.locationsDbVer.get();
                    if (assetsVersion > version) {
                        replaceDatabase(context);
                        preferences.locationsDbVer.put(assetsVersion);
                        preferences.locations.put(Collections.emptySet());
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    private static void replaceDatabase(Context context) throws IOException {
        try (InputStream iS = context.getAssets().open(DB_NAME)) {
            File dbFile = context.getDatabasePath(DB_NAME);
            try (FileOutputStream oS = new FileOutputStream(dbFile, false)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = iS.read(buffer)) != -1) {
                    oS.write(buffer, 0, length);
                }
            }
        }
    }
}
