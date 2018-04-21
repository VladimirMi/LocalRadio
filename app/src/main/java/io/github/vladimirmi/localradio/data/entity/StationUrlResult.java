package io.github.vladimirmi.localradio.data.entity;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */


public class StationUrlResult {

    @Json(name = "success") private boolean success;
    @Json(name = "result") private List<Station> result;

    public boolean isSuccess() {
        return success;
    }

    //todo replace Station with String url
    public List<Station> getResult() {
        return result;
    }
}
