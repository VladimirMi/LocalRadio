package io.github.vladimirmi.localradio.data.db.favorite;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

/**
 * Created by Vladimir Mikhalev 21.05.2018.
 */
@Dao
public interface StationsDao {

    @Insert
    void insertStation(StationEntity stationEntity);

    @Query("DELETE from favorite_stations WHERE id = :id")
    void deleteStation(int id);

    @Query("SELECT * from favorite_stations")
    Flowable<List<StationEntity>> getStations();
}
