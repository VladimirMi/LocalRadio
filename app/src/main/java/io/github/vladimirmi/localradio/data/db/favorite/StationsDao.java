package io.github.vladimirmi.localradio.data.db.favorite;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
