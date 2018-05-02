package io.github.vladimirmi.localradio.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

@Singleton
public class LocationSource {

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final Geocoder geocoder;

    @Inject
    public LocationSource(Context context) {
        // TODO: 4/6/18 check if play services available
//        Timber.e("LocationSource: " + GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context));
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    public Single<Pair<Float, Float>> getCoordinates() {
        return getLastLocation()
                .map(location -> {
                    float latitude = Math.round(location.getLatitude() * 100.0) / 100.0f;
                    float longitude = Math.round(location.getLongitude() * 100.0) / 100.0f;
                    return new Pair<>(latitude, longitude);
                });
    }

    @Nullable
    public Pair<String, String> getCountryCodeCity(Pair<Float, Float> coordinates) {
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(coordinates.first, coordinates.second, 1);
        } catch (IOException e) {
            Timber.w("Service not available");
        } catch (IllegalArgumentException e) {
            Timber.w("Invalid latitude or longitude values (%s, %s)", coordinates.first, coordinates.second);
        }
        if (addresses != null && !addresses.isEmpty()) {
            return getCountryCodeCity(addresses.get(0));
        } else {
            Timber.w("Can not find address (%s, %s)", coordinates.first, coordinates.second);
            return null;
        }
    }

    @SuppressLint("MissingPermission")
    private Single<Location> getLastLocation() {
        return Single.create(emitter -> fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (!emitter.isDisposed() && location != null) emitter.onSuccess(location);
                })
                .addOnFailureListener(e -> {
                    if (!emitter.isDisposed()) emitter.onError(e);
                }));
    }

    private Pair<String, String> getCountryCodeCity(Address address) {
        String countryCode = address.getCountryCode();
        String city = address.getLocality() != null ? address.getLocality() : "";

        return new Pair<>(countryCode, city);
    }
}
