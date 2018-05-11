package io.github.vladimirmi.localradio.data.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 06.04.2018.
 */

@Singleton
public class LocationSource {

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final Geocoder geocoder;
    private final Context context;
    private final LocationRequest locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_LOW_POWER)
            .setInterval(2000)
            .setFastestInterval(1000)
            .setNumUpdates(1);

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationSource(Context context) {
        this.context = context;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        geocoder = new Geocoder(context, Locale.getDefault());
    }

    public boolean isServicesAvailable() {
        // TODO: 5/11/18 return single with various message errors
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                == ConnectionResult.SUCCESS;
    }

    public Completable checkCanGetLocation() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(context.getApplicationContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        return Completable.create(emitter -> {
            task.addOnSuccessListener(locationSettingsResponse -> {
                if (!emitter.isDisposed()) emitter.onComplete();
            });

            task.addOnFailureListener(e -> {
                if (!emitter.isDisposed()) emitter.onError(e);
            });
        });
    }

    public Single<Pair<Float, Float>> getCoordinates() {
        return getLocation()
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
    private Single<Location> getLocation() {

        return Single.create(emitter -> {
            LocationCallback locationCallback = new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    emitter.onSuccess(locationResult.getLastLocation());
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    Timber.w("onLocationAvailability: %s", locationAvailability);
                    if (!locationAvailability.isLocationAvailable()) {
                        emitter.onError(new NoSuchElementException());
                    }
                }
            };
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.getMainLooper());

            emitter.setDisposable(Disposables.fromRunnable(() -> fusedLocationProviderClient
                    .removeLocationUpdates(locationCallback)));
        });
    }


    private Pair<String, String> getCountryCodeCity(Address address) {
        String countryCode = address.getCountryCode();
        String city = address.getLocality() != null ? address.getLocality() : "";
        Timber.i("getCountryCodeCity: %s, %s", countryCode, city);
        return new Pair<>(countryCode, city);
    }
}
