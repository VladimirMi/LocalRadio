package io.github.vladimirmi.localradio.data.entity;

import com.squareup.moshi.Json;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class Station {

    @Json(name = "station_id") private int id;
    @Json(name = "callsign") private String callsign;
    @Json(name = "band") private String band;
    @Json(name = "ubergenre") private String genre;
    @Json(name = "language") private String language;
    @Json(name = "websiteurl") private String websiteurl;
    @Json(name = "imageurl") private String imageurl;
    @Json(name = "description") private String description;
    @Json(name = "encoding") private String encoding;
    @Json(name = "status") private String status;
    @Json(name = "country") private String countryCode;
    @Json(name = "city") private String city;
    @Json(name = "phone") private String phone;
    @Json(name = "email") private String email;
    @Json(name = "dial") private String dial;
    @Json(name = "slogan") private String slogan;
    @Json(name = "url") private String url;

    public String getBand() {
        return band;
    }

    public String getGenre() {
        return genre;
    }

    public String getLanguage() {
        return language;
    }

    public String getWebsiteurl() {
        return websiteurl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public String getDescription() {
        return description;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getStatus() {
        return status;
    }

    public String getCity() {
        return city;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCallsign() {
        return callsign;
    }

    public String getDial() {
        return dial;
    }

    public int getId() {
        return id;
    }

    public String getSlogan() {
        return slogan;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public static Station nullStation() {
        return new Station();
    }

    public boolean isNullStation() {
        return id == 0;
    }
}
