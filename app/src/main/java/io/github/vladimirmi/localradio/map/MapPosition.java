package io.github.vladimirmi.localradio.map;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
// TODO: 27.10.18 Remove class
public class MapPosition {

    public final float latitude;
    public final float longitude;
    public final float zoom;

    public MapPosition(float latitude, float longitude, float zoom) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoom = zoom;
    }

    public MapPosition(LatLng center, float zoom) {
        latitude = (float) center.latitude;
        longitude = (float) center.longitude;
        this.zoom = zoom;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
