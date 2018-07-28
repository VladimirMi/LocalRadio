package io.github.vladimirmi.localradio.map;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Vladimir Mikhalev 20.07.2018.
 */
// TODO: 7/20/18 write javadocs
public class MapUtils {

    private MapUtils() {
    }

    /**
     * Rounds the value to the 4th decimal place
     */
    public static double round(double a) {
        return Math.round(a * 10000.0) / 10000.0;
    }

    /**
     * Calculates distance in degrees between two coordinates on the same longitude or latitude
     *
     * @param x1 latitude1 or longitude1
     * @param x2 latitude2 or longitude2 (accordingly)
     */
    public static double delta(double x1, double x2) {
        double a = round(x1);
        double b = round(x2);
        double d1 = Math.abs(a - b);
        double d2 = Math.abs(360 + a - b);
        double d3 = Math.abs(360 + b - a);
        return Math.min(Math.min(d1, d2), d3);
    }

    /**
     * Calculates distance in degrees between two points
     */
    public static double distanceDegrees(LatLng latLng1, LatLng latLng2) {
        double deltaLongitude = delta(latLng1.longitude, latLng2.longitude);
        double deltaLatitude = delta(latLng1.latitude, latLng2.latitude);

        return Math.sqrt(deltaLatitude * deltaLatitude + deltaLongitude * deltaLongitude);
    }

    /**
     * Calculate distance between two points in latitude and longitude.
     * Uses Haversine method as its base.
     *
     * @return Distance in miles
     */
    public static double distanceMiles(LatLng latLng1, LatLng latLng2) {

        final int R = 3960; // Radius of the earth in statute miles

        double latDistance = Math.toRadians(latLng2.latitude - latLng1.latitude);
        double lonDistance = Math.toRadians(latLng2.longitude - latLng1.longitude);
        double a = Math.pow(Math.sin(latDistance / 2), 2)
                + Math.cos(Math.toRadians(latLng1.latitude))
                * Math.cos(Math.toRadians(latLng2.latitude))
                * Math.pow(Math.sin(lonDistance / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static SupportSQLiteQuery createQueryFor(Bounds bounds, boolean isCountry) {
        //noinspection StringBufferReplaceableByString
        String sb = new StringBuilder()
                .append("SELECT * FROM locations WHERE ")
                .append("endpoints ").append(isCountry ? "==" : "!=").append(" 'isCountry'")
                .append(" AND latitude >= ").append(bounds.bottom)
                .append(" AND latitude <= ").append(bounds.top)
                .append(" AND longitude >= ").append(bounds.left)
                .append(" AND longitude <= ").append(bounds.right)
                .toString();

        return new SimpleSQLiteQuery(sb);
    }
}
