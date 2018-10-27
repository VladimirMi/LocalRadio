package io.github.vladimirmi.localradio.domain.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;

/**
 * Created by Vladimir Mikhalev 03.07.2018.
 */
public class LocationClusterItem implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet = null;
    private final int id;
    private final int stationsNum;

    public LocationClusterItem(LocationEntity location) {
        position = new LatLng(location.latitude, location.longitude);
        stationsNum = location.stations;
        if (location.isCountry()) {
            title = location.name;
        } else {
            title = String.format("%s, %s", location.name, location.country);
        }
        id = location.id;
    }

    public static LocationClusterItem empty() {
        return new LocationClusterItem(new LocationEntity());
    }

    public boolean isEmpty() {
        return stationsNum == 0;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public int getId() {
        return id;
    }

    public int getStationsNum() {
        return stationsNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationClusterItem that = (LocationClusterItem) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
