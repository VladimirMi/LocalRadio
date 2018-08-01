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
    private int stationsNum;

    public LocationClusterItem(LocationEntity location) {
        position = new LatLng(location.latitude, location.longitude);
        stationsNum = location.stations;
        if (location.isCountry()) {
            title = location.name;
        } else {
            title = String.format("%s, %s", location.name, location.country);
        }
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

    public int getStationsNum() {
        return stationsNum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationClusterItem that = (LocationClusterItem) o;

        return position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }
}
