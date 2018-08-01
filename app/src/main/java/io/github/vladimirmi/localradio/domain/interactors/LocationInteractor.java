package io.github.vladimirmi.localradio.domain.interactors;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.map.MapState;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 24.04.2018.
 */
public class LocationInteractor {

    private final LocationRepository locationRepository;
    @Nullable private LocationEntity countryLocation;
    @Nullable private LocationEntity cityLocation;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationInteractor(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public String getMapMode() {
        return locationRepository.getMapMode();
    }

    public void saveMapMode(String mode) {
        locationRepository.saveMapMode(mode);
    }

    public MapState getMapState() {
        return locationRepository.getMapState();
    }

    public void saveMapState(MapState mapState) {
        locationRepository.saveMapState(mapState);
    }

    public void saveAutodetect(boolean enabled) {
        locationRepository.saveAutodetect(enabled);
    }

    public boolean isAutodetect() {
        return locationRepository.isAutodetect();
    }

    public Single<List<LocationEntity>> getCountries() {
        return locationRepository.getCountries();
    }

    public Single<List<LocationEntity>> getCities(String countryCode) {
        return locationRepository.getCities(countryCode);
    }

    public Single<LocationEntity> getCountry(String countryCode) {
        return locationRepository.getCountry(countryCode);
    }

    public void saveLocations(Set<LocationClusterItem> locations) {
        Set<String> ids = new HashSet<>();
        for (LocationClusterItem location : locations) {
            ids.add(String.valueOf(location.getId()));
        }
        locationRepository.saveLocations(ids);
    }

    public LocationEntity saveCountryLocation(@Nullable LocationEntity location) {
        countryLocation = location;
        LocationEntity currentLocation;
        if ((countryLocation != null && cityLocation != null
                && countryLocation.country.equals(cityLocation.country)) ||
                (countryLocation == null && cityLocation != null)) {

            currentLocation = cityLocation;
        } else {
            currentLocation = countryLocation;
            cityLocation = null;
        }
        saveLocation(currentLocation);
        return currentLocation;
    }

    public LocationEntity saveCityLocation(@Nullable LocationEntity location) {
        cityLocation = location;
        LocationEntity currentLocation;
        if (cityLocation == null && countryLocation != null) {

            currentLocation = countryLocation;
        } else {
            currentLocation = cityLocation;
        }
        saveLocation(currentLocation);
        return currentLocation;
    }

    public Single<Set<LocationClusterItem>> getSavedLocations() {
        return locationRepository.getSavedLocations()
                .flattenAsObservable(locationEntities -> locationEntities)
                .map(LocationClusterItem::new)
                .collect(HashSet::new, Set::add);
    }

    public Single<LocationEntity> getSavedLocation() {
        return locationRepository.getSavedLocations()
                .filter(locationEntities -> !locationEntities.isEmpty())
                .toSingle()
                .map(locationEntities -> locationEntities.get(0))
                .doOnSuccess(location -> {
                    if (location.isCountry()) countryLocation = location;
                    else cityLocation = location;
                });
    }

    public Single<Set<LocationClusterItem>> loadClusters(SupportSQLiteQuery query) {
        return locationRepository.loadClusters(query)
                .flattenAsObservable(locationEntities -> locationEntities)
                .map(LocationClusterItem::new)
                .collect(HashSet::new, Set::add);
    }

    public Completable checkCanSearch() {
//        if (getCountryName().isEmpty() && getCity().isEmpty()) {
//            return Completable.error(new MessageException(R.string.error_specify_location));
//        } else {
        return Completable.complete();
//        }
    }

    public boolean isServicesAvailable() {
        return locationRepository.isServicesAvailable();
    }

    private void saveLocation(LocationEntity currentLocation) {
        Set<String> ids;
        if (currentLocation != null) {
            ids = Collections.singleton(String.valueOf(currentLocation.id));
        } else {
            ids = Collections.emptySet();
        }
        locationRepository.saveLocations(ids);
    }
}
