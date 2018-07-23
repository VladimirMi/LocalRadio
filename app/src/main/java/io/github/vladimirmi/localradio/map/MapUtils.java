package io.github.vladimirmi.localradio.map;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;

/**
 * Created by Vladimir Mikhalev 20.07.2018.
 */
// TODO: 7/20/18 write javadocs
public class MapUtils {

    private MapUtils() {
    }

    public static double delta(double x1, double x2) {
        double a = round(x1);
        double b = round(x2);
        double d1 = Math.abs(a - b);
        double d2 = Math.abs(360 + a - b);
        double d3 = Math.abs(360 + b - a);
        return Math.min(Math.min(d1, d2), d3);
    }

    public static double round(double a) {
        return Math.round(a * 10000.0) / 10000.0;
    }

    public static SupportSQLiteQuery createQueryFor(Bounds bounds, boolean isCountry) {
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
