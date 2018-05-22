package io.github.vladimirmi.localradio.data.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Vladimir Mikhalev 21.05.2018.
 */
@Entity(tableName = "favorite_stations")
public class StationEntity {

    @PrimaryKey
    public int id;
    public String name;
    public String band;
    public String genre;
    public String language;
    public String websiteUrl;
    public String imageUrl;
    public String description;
    public String encoding;
    public String status;
    public String countryCode;
    public String city;
    public String phone;
    public String email;
    public String dial;
    public String slogan;
}
