package io.github.vladimirmi.localradio.domain.models;

import io.github.vladimirmi.localradio.data.db.favorite.StationEntity;
import io.github.vladimirmi.localradio.data.models.StationRes;
import io.github.vladimirmi.localradio.data.net.Api;

/**
 * Created by Vladimir Mikhalev 22.05.2018.
 */
public class Station {

    public final int id;
    public final String name;
    public final String band;
    public final String genre;
    public final String language;
    public final String websiteUrl;
    public final String imageUrl;
    public final String description;
    public final String encoding;
    public final String status;
    public final String countryCode;
    public final String city;
    public final String phone;
    public final String email;
    public final String dial;
    public final String slogan;

    public final String url;
    public final String bandString;
    public final String locationString;
    public final boolean isNullObject;

    public Station(StationRes stationRes) {
        id = stationRes.id;
        name = stationRes.name;
        band = stationRes.band;
        genre = stationRes.genre;
        language = stationRes.language;
        websiteUrl = stationRes.websiteUrl;
        imageUrl = stationRes.imageUrl;
        description = stationRes.description;
        encoding = stationRes.encoding;
        status = stationRes.status;
        countryCode = stationRes.countryCode;
        city = stationRes.city;
        phone = stationRes.phone;
        email = stationRes.email;
        dial = stationRes.dial;
        slogan = stationRes.slogan;

        url = getUrl();
        bandString = getBandString();
        locationString = getLocationString();
        isNullObject = isNullObject();
    }

    public Station(StationEntity stationEntity) {
        id = stationEntity.id;
        name = stationEntity.name;
        band = stationEntity.band;
        genre = stationEntity.genre;
        language = stationEntity.language;
        websiteUrl = stationEntity.websiteUrl;
        imageUrl = stationEntity.imageUrl;
        description = stationEntity.description;
        encoding = stationEntity.encoding;
        status = stationEntity.status;
        countryCode = stationEntity.countryCode;
        city = stationEntity.city;
        phone = stationEntity.phone;
        email = stationEntity.email;
        dial = stationEntity.dial;
        slogan = stationEntity.slogan;

        url = getUrl();
        bandString = getBandString();
        locationString = getLocationString();
        isNullObject = isNullObject();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Station station = (Station) o;

        return id == station.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Station{" + "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    private String getUrl() {
        return Api.STREAM_BASE_URL + id;
    }

    private String getBandString() {
        if (band.equals("net")) {
            return band;
        } else {
            return String.format("%s %s", dial, band);
        }
    }

    private boolean isNullObject() {
        return id == 0;
    }

    private String getLocationString() {
        if (city.equals("{unlisted}") || city.isEmpty()) {
            return countryCode;
        } else {
            return String.format("%s, %s", city, countryCode);
        }
    }

    public static Station nullObject() {
        return new Station(new StationRes());
    }
}
