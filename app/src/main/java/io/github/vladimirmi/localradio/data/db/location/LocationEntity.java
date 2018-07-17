package io.github.vladimirmi.localradio.data.db.location;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Vladimir Mikhalev 29.06.2018.
 */
@Entity(tableName = "locations")
public class LocationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public float latitude;
    public float longitude;
    public String country;
    public String endpoints;
    public int stations;

    public LocationEntity() {
    }

    @Override
    public String toString() {
        return name;
    }
}

