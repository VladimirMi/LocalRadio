package io.github.vladimirmi.localradio.data.net;

import io.github.vladimirmi.localradio.data.entity.StationsResult;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Rest service interface
 */

public interface RestService {

    @GET("darstations.php")
    Single<StationsResult> getStationsByCoordinates(@Query("latitude") float latitude,
                                                    @Query("longitude") float longitude);

    @GET("darstations.php")
    Single<StationsResult> getStationsByIp(@Query("ip") String ip);

    @GET("darstations.php")
    Single<StationsResult> getStationsByLocation(@Query("country") String countryCode,
                                                 @Query("city") String city,
                                                 @Query("exact") int exact);
}
