package io.github.vladimirmi.localradio.data.models;

import com.squareup.moshi.Json;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationRes {

    @Json(name = "station_id") public int id;
    @Json(name = "callsign") public String name = "";
    @Json(name = "band") public String band = "";
    @Json(name = "ubergenre") public String genre = "";
    @Json(name = "language") public String language = "";
    @Json(name = "websiteurl") public String websiteUrl = "";
    @Json(name = "imageurl") public String imageUrl = "";
    @Json(name = "description") public String description = "";
    @Json(name = "encoding") public String encoding = "";
    @Json(name = "status") public String status = "";
    @Json(name = "country") public String countryCode = "";
    @Json(name = "city") public String city = "";
    @Json(name = "phone") public String phone = "";
    @Json(name = "email") public String email = "";
    @Json(name = "dial") public String dial = "";
    @Json(name = "slogan") public String slogan = "";
}
