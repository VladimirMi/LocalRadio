package io.github.vladimirmi.localradio.data.repositories;

import android.util.Pair;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationDatabase;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.data.db.location.LocationsDao;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.source.LocationSource;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 03.04.2018.
 */

public class LocationRepositoryImpl implements LocationRepository {

    private final Preferences preferences;
    private final LocationSource locationSource;
    private final LocationsDao locationsDao;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationRepositoryImpl(Preferences preferences,
                                  LocationSource locationSource,
                                  LocationDatabase database) {
        this.preferences = preferences;
        this.locationSource = locationSource;
        this.locationsDao = database.locationsDao();
    }

    @Override
    public Observable<List<LocationEntity>> getCountries() {
        return locationsDao.findCountries();
    }

    @Override
    public void saveMapMode(String mode) {
        preferences.mapMode.put(mode);
    }

    @Override
    public String getMapMode() {
        return preferences.mapMode.get();
    }

    @Override
    public Observable<List<LocationEntity>> getCountry(String isoCode) {
        return null;
    }

    @Override
    public Observable<List<LocationEntity>> getCities() {
        return null;
    }

    @Override
    public Observable<List<LocationEntity>> getCities(String country) {
        return null;
    }

    @Override
    public void saveLocations(int... locationId) {

    }

    @Override
    public Observable<List<LocationEntity>> getSavedLocations() {
        return null;
    }

    @Override
    public void saveAutodetect(boolean enabled) {
        preferences.autodetect.put(enabled);
    }

    @Override
    public boolean isAutodetect() {
        return preferences.autodetect.get();
    }

    @Override
    public boolean isServicesAvailable() {
        return locationSource.isServicesAvailable();
    }


    @Override
    public Completable checkCanGetLocation() {
        return locationSource.checkCanGetLocation();
    }

    @Override
    public Single<Pair<Float, Float>> getCurrentLocation() {
        return locationSource.getCoordinates();
    }
}
