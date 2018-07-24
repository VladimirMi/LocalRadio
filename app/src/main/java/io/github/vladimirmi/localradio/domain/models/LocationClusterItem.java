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
    private final String snippet;
    private int stationsNum;

    public LocationClusterItem(LocationEntity location) {
        this.position = new LatLng(location.latitude, location.longitude);
        this.title = location.name;
        this.snippet = String.valueOf(location.id);
        this.stationsNum = location.stations;
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
}
