package io.github.vladimirmi.localradio.data.net;

import io.github.vladimirmi.localradio.data.entity.StationUrlResult;
import io.github.vladimirmi.localradio.data.entity.StationsResult;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Rest service interface
 */

public interface RestService {

    @GET("darstations.php")
    Single<StationsResult> getStationsByCoordinates(@Query("latitude") double latitude,
                                                    @Query("longitude") double longitude);

    @GET("darstations.php")
    Single<StationsResult> getStationsByIp(@Query("ip") String ip);

    @GET("darstations.php")
    Single<StationsResult> getStationsByLocation(@Query("country") String countryCode,
                                                 @Query("city") String city,
                                                 @Query("exact") int exact);

    @GET("uberstationurl.php")
    Single<StationUrlResult> getStationUrl(@Query("station_id") int stationId);
}
