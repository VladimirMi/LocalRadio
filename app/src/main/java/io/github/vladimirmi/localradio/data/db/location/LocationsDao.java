package io.github.vladimirmi.localradio.data.db.location;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 03.07.2018.
 */
@Dao
public interface LocationsDao {

    @Query("SELECT * FROM locations WHERE state == 'isCountry'")
    Observable<List<LocationEntity>> findCountries();

    @Query("SELECT * FROM locations WHERE state != 'isCountry'")
    Observable<List<LocationEntity>> findCities();
}
