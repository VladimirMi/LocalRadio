package io.github.vladimirmi.localradio.data.db.favorite;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Vladimir Mikhalev 23.05.2018.
 */
@Database(entities = {StationEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract StationsDao stationsDao();

    public static AppDatabase getInstance(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "database").build();
    }
}
