package io.github.vladimirmi.localradio.data.db.location;

import androidx.sqlite.db.SupportSQLiteQuery;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.RawQuery;

import java.util.List;

import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 03.07.2018.
 */
@Dao
public interface LocationsDao {

    @Query("SELECT * FROM locations WHERE endpoints == 'isCountry' ORDER BY name")
    Single<List<LocationEntity>> getCountries();

    @Query("SELECT * FROM locations WHERE endpoints != 'isCountry' ORDER BY name")
    Single<List<LocationEntity>> getCities();

    @Query("SELECT * FROM locations WHERE endpoints == 'isCountry' AND country == :isoCode")
    Single<LocationEntity> getCountry(String isoCode);

    @Query("SELECT * FROM locations WHERE endpoints != 'isCountry' AND country == :isoCode ORDER BY name")
    Single<List<LocationEntity>> getCities(String isoCode);

    @Query("SELECT * FROM locations WHERE id == :id")
    Single<LocationEntity> getLocation(int id);

    @RawQuery()
    Single<List<LocationEntity>> query(SupportSQLiteQuery query);
}
