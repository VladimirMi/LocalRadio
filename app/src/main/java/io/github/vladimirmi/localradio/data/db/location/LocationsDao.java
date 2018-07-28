package io.github.vladimirmi.localradio.data.db.location;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

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

    @RawQuery()
    Single<List<LocationEntity>> query(SupportSQLiteQuery query);
}
