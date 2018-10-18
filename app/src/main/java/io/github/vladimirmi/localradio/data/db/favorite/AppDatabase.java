package io.github.vladimirmi.localradio.data.db.favorite;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Vladimir Mikhalev 23.05.2018.
 */
@Database(entities = {StationEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract StationsDao stationsDao();

    public static AppDatabase getInstance(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "database").build();
    }
}
