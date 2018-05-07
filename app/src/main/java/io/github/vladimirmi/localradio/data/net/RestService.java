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
    Single<StationsResult> getStationsByCoordinates(@Query(Api.QUERY_LATITUDE) float latitude,
                                                    @Query(Api.QUERY_LONGITUDE) float longitude);

    @GET("darstations.php")
    Single<StationsResult> getStationsByIp(@Query(Api.QUERY_IP) String ip);

    @GET("darstations.php")
    Single<StationsResult> getStationsByLocation(@Query(Api.QUERY_COUNTRY) String countryCode,
                                                 @Query(Api.QUERY_CITY) String city,
                                                 @Query("exact") int exact);
}
