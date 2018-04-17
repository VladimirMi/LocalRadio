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
    private boolean isFavorite;

    public Station() {
    }

    public Station(int id, String callsign, String band, String genre, String language,
                   String websiteurl, String imageurl, String description, String encoding,
                   String status, String countryCode, String city, String phone, String email,
                   String dial, String slogan, String url, boolean isFavorite) {
        this.id = id;
        this.callsign = callsign;
        this.band = band;
        this.genre = genre;
        this.language = language;
        this.websiteurl = websiteurl;
        this.imageurl = imageurl;
        this.description = description;
        this.encoding = encoding;
        this.status = status;
        this.countryCode = countryCode;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.dial = dial;
        this.slogan = slogan;
        this.url = url;
        this.isFavorite = isFavorite;
    }

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

    public boolean isFavorite() {
        return isFavorite;
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
        return "Station{" +
                "id=" + id +
                ", callsign='" + callsign + '\'' +
                ", band='" + band + '\'' +
                ", genre='" + genre + '\'' +
                ", language='" + language + '\'' +
                ", websiteurl='" + websiteurl + '\'' +
                ", imageurl='" + imageurl + '\'' +
                ", description='" + description + '\'' +
                ", encoding='" + encoding + '\'' +
                ", status='" + status + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", city='" + city + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", dial='" + dial + '\'' +
                ", slogan='" + slogan + '\'' +
                ", url='" + url + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }

    public static Station nullStation() {
        return new Station();
    }

    public boolean isNullStation() {
        return id == 0;
    }

    public Station setFavorite(boolean isFavorite) {
        return new Station(id, callsign, band, genre, language, websiteurl, imageurl, description,
                encoding, status, countryCode, city, phone, email, dial, slogan, url, isFavorite);
    }

    public Station setUrl(String url) {
        return new Station(id, callsign, band, genre, language, websiteurl, imageurl, description,
                encoding, status, countryCode, city, phone, email, dial, slogan, url, isFavorite);
    }
}
