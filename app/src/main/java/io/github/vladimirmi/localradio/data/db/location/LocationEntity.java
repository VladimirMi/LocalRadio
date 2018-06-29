package io.github.vladimirmi.localradio.data.db.location;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Vladimir Mikhalev 29.06.2018.
 */
@Entity(tableName = "locations")
class LocationEntity {

    @PrimaryKey
    public int id;

}
