package io.github.vladimirmi.localradio.domain.interactors;

import android.arch.persistence.db.SupportSQLiteQuery;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.map.MapState;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 24.04.2018.
 */
public class LocationInteractor {

    private final LocationRepository locationRepository;
    private Set<LocationClusterItem> selectedMapLocations;
    private LocationEntity selectedManualLocation;
    private MapState mapState;
    private String mapMode;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationInteractor(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
        mapMode = locationRepository.getMapMode();
        mapState = locationRepository.getMapState();
    }

    public String getMapMode() {
        return mapMode;
    }

    public void setMapMode(String mode) {
        mapMode = mode;
    }

    public MapState getMapState() {
        return mapState;
    }

    public void setMapState(MapState mapState) {
        this.mapState = mapState;
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

    public void setSelectedMapLocations(Set<LocationClusterItem> selectedMapLocations) {
        this.selectedMapLocations = selectedMapLocations;
    }

    public void setSelectedManualLocation(LocationEntity selectedManualLocation) {
        this.selectedManualLocation = selectedManualLocation;
    }

    public boolean saveMapSelection() {
        Set<String> ids = new HashSet<>();
        for (LocationClusterItem location : selectedMapLocations) {
            ids.add(String.valueOf(location.getId()));
        }
        if (ids.isEmpty()) return false;
        locationRepository.saveLocations(ids);
        locationRepository.saveMapMode(mapMode);
        locationRepository.saveMapState(mapState);
        return true;
    }

    public boolean saveManualSelection() {
        Set<String> ids;
        if (selectedManualLocation != null) {
            ids = Collections.singleton(String.valueOf(selectedManualLocation.id));
        } else {
            ids = Collections.emptySet();
        }
        if (ids.isEmpty()) return false;
        locationRepository.saveLocations(ids);
        locationRepository.saveMapMode(mapMode);
        locationRepository.saveMapState(mapState);
        return true;
    }

    public Single<Set<LocationClusterItem>> getSavedLocations() {
        return locationRepository.getSavedLocations()
                .flattenAsObservable(locationEntities -> locationEntities)
                .map(LocationClusterItem::new)
                .<Set<LocationClusterItem>>collect(HashSet::new, Set::add)
                .doOnSuccess(locations -> selectedMapLocations = locations);
    }

    public Single<LocationEntity> getSavedLocation() {
        return locationRepository.getSavedLocations()
                .filter(locationEntities -> !locationEntities.isEmpty())
                .toSingle()
                .map(locationEntities -> locationEntities.get(0))
                .doOnSuccess(location -> selectedManualLocation = location);
    }

    public Single<Set<LocationClusterItem>> loadClusters(SupportSQLiteQuery query) {
        return locationRepository.loadClusters(query)
                .flattenAsObservable(locationEntities -> locationEntities)
                .map(LocationClusterItem::new)
                .collect(HashSet::new, Set::add);
    }

    public boolean isServicesAvailable() {
        return locationRepository.isServicesAvailable();
    }
}
