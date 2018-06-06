package io.github.vladimirmi.localradio.data.models;

import com.squareup.moshi.Json;

import java.util.Collections;
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

    public List<StationRes> getStations() {
        return success ? result.get(0).getStations() : Collections.emptyList();
    }

    private static class ResultBean {

        @Json(name = "stations") private List<StationRes> stations;

        public List<StationRes> getStations() {
            return stations;
        }
    }
}
