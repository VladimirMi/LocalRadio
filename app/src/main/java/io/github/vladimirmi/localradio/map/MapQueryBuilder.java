package io.github.vladimirmi.localradio.map;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;

/**
 * Created by Vladimir Mikhalev 20.07.2018.
 */
public class MapQueryBuilder {

    // TODO: 7/20/18 table name (locations) to const
    private final StringBuilder sb = new StringBuilder();

    public SupportSQLiteQuery build() {
        return new SimpleSQLiteQuery(sb.toString());
    }

    public MapQueryBuilder insideBounds(Bounds bounds) {
        if (ensureInit()) sb.append(" AND ");

        sb.append("latitude >= ").append(MapUtils.toLatitude(bounds.top))
                .append(" AND ").append("latitude <= ").append(MapUtils.toLatitude(bounds.bottom))
                .append(" AND ").append("longitude >= ").append(MapUtils.toLongitude(bounds.right))
                .append(" AND ").append("longitude <= ").append(MapUtils.toLatitude(bounds.left));
        return this;
    }

    public MapQueryBuilder isCountry(boolean isCountry) {
        if (ensureInit()) sb.append(" AND ");

        sb.append("endpoints").append(isCountry ? " == " : " != ").append("'isCountry'");
        return this;
    }

    private boolean ensureInit() {
        if (sb.length() == 0) {
            sb.append("SELECT * FROM locations WHERE ");
            return false;
        } else {
            return true;
        }
    }

}
