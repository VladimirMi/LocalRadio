package io.github.vladimirmi.localradio.data.db.favorite;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import io.github.vladimirmi.localradio.domain.models.Station;

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

    public StationEntity() {
    }

    public StationEntity(Station station) {
        this.id = station.id;
        this.name = station.name;
        this.band = station.band;
        this.genre = station.genre;
        this.language = station.language;
        this.websiteUrl = station.websiteUrl;
        this.imageUrl = station.imageUrl;
        this.description = station.description;
        this.encoding = station.encoding;
        this.status = station.status;
        this.countryCode = station.countryCode;
        this.city = station.city;
        this.phone = station.phone;
        this.email = station.email;
        this.dial = station.dial;
        this.slogan = station.slogan;
    }
}
