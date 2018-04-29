package io.github.vladimirmi.localradio.data.entity;

import com.squareup.moshi.Json;

import io.github.vladimirmi.localradio.data.net.Api;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class Station {

    @Json(name = "station_id") private int id;
    @Json(name = "callsign") private String name;
    @Json(name = "band") private String band;
    @Json(name = "ubergenre") private String genre;
    @Json(name = "language") private String language;
    @Json(name = "websiteurl") private String websiteUrl;
    @Json(name = "imageurl") private String imageUrl;
    @Json(name = "description") private String description;
    @Json(name = "encoding") private String encoding;
    @Json(name = "status") private String status;
    @Json(name = "country") private String countryCode;
    @Json(name = "city") private String city;
    @Json(name = "phone") private String phone;
    @Json(name = "email") private String email;
    @Json(name = "dial") private String dial;
    @Json(name = "slogan") private String slogan;
    private boolean isFavorite;
    private String url;

    public Station() {
    }

    public Station(int id, String name, String band, String genre, String language,
                   String websiteUrl, String imageUrl, String description, String encoding,
                   String status, String countryCode, String city, String phone, String email,
                   String dial, String slogan, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.band = band;
        this.genre = genre;
        this.language = language;
        this.websiteUrl = websiteUrl;
        this.imageUrl = imageUrl;
        this.description = description;
        this.encoding = encoding;
        this.status = status;
        this.countryCode = countryCode;
        this.city = city;
        this.phone = phone;
        this.email = email;
        this.dial = dial;
        this.slogan = slogan;
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

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public String getImageUrl() {
        return imageUrl;
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

    public String getName() {
        return name;
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

    public boolean isFavorite() {
        return isFavorite;
    }

    public String getUrl() {
        if (id == 0) return null;
        if (url == null) url = Api.STREAM_BASE_URL + id;
        return url;
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
                ", name='" + name + '\'' +
                ", band='" + band + '\'' +
                ", genre='" + genre + '\'' +
                ", language='" + language + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", encoding='" + encoding + '\'' +
                ", status='" + status + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", city='" + city + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", dial='" + dial + '\'' +
                ", slogan='" + slogan + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }

    public static Station nullStation() {
        return new Station();
    }

    public boolean isNullStation() {
        return id == 0;
    }

    public Station copy(boolean isFavorite) {
        return new Station(id, name, band, genre, language, websiteUrl, imageUrl, description,
                encoding, status, countryCode, city, phone, email, dial, slogan, isFavorite);
    }
}
