package io.github.vladimirmi.localradio.map;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Vladimir Mikhalev 24.07.2018.
 */
public class MapState {

    public final float latitude;
    public final float longitude;
    public final float zoom;

    public MapState(float latitude, float longitude, float zoom) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoom = zoom;
    }

    public MapState(LatLng center, float zoom) {
        latitude = (float) center.latitude;
        longitude = (float) center.longitude;
        this.zoom = zoom;
    }
}
