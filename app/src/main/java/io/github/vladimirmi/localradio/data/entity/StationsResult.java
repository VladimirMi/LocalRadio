package io.github.vladimirmi.localradio.data.entity;

import com.squareup.moshi.Json;

import java.util.List;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class StationsResult {

    @Json(name = "success") private boolean success;
    @Json(name = "result") private List<ResultBean> result;

    public boolean isSuccess() {
        return success;
    }

    public List<Station> getStations() {
        return result.get(0).getStations();
    }

    private static class ResultBean {

        @Json(name = "stations") private List<Station> stations;

        public List<Station> getStations() {
            return stations;
        }
    }
}
