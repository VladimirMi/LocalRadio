package io.github.vladimirmi.localradio.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Vladimir Mikhalev 20.07.2018.
 */
// TODO: 7/20/18 write javadocs
public class MapUtils {

    private MapUtils() {
    }

    public static Point normalize(LatLng latLng) {
        return new Point(normalizeLat(latLng.latitude), normalizeLong(latLng.longitude));
    }

    public static Bounds normalize(LatLngBounds bounds) {
        return new Bounds(
                normalizeLat(bounds.northeast.latitude),
                normalizeLong(bounds.northeast.longitude),
                normalizeLat(bounds.southwest.latitude),
                normalizeLong(bounds.southwest.longitude));
    }

    public static double delta(double a, double b) {
        double d1 = Math.abs(a - b);
        double d2 = Math.abs(360 + a - b);
        double d3 = Math.abs(360 + b - a);
        return Math.min(Math.min(d1, d2), d3);
    }

    private static double normalizeLat(double latitude) {
        return 90 - latitude;
    }

    public static double toLatitude(double normLat) {
        return 90 - normLat;
    }

    private static double normalizeLong(double longitude) {
        if (longitude >= 0) {
            return longitude;
        } else {
            return 360 + longitude;
        }
    }

    public static double toLongitude(double normLong) {
        if (normLong <= 180) {
            return normLong;
        } else {
            return normLong - 360;
        }
    }
}
