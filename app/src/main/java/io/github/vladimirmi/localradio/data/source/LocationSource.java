package io.github.vladimirmi.localradio.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Inject;

import io.reactivex.Single;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

public class LocationSource {

    private final FusedLocationProviderClient fusedLocationProviderClient;

    @Inject
    public LocationSource(Context context) {
        // TODO: 4/6/18 check if play services available
        Timber.e("LocationSource: " + GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context));
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public Single<Location> getLastLocation() {
        return Single.create(emitter -> fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    Timber.e("getLastLocation: " + location);
                    if (!emitter.isDisposed() && location != null) emitter.onSuccess(location);
                })
                .addOnFailureListener(e -> {
                    if (!emitter.isDisposed()) emitter.onError(e);
                }));
    }
}
