package io.github.vladimirmi.localradio.domain.interactors;

import android.util.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.sqlite.db.SupportSQLiteQuery;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.models.LocationClusterItem;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
import io.github.vladimirmi.localradio.map.Bounds;
import io.github.vladimirmi.localradio.map.MapPosition;
import io.github.vladimirmi.localradio.map.MapUtils;
import io.github.vladimirmi.localradio.map.MapWrapper;
import io.github.vladimirmi.localradio.presentation.search.SearchPresenter;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 24.04.2018.
 */
public class LocationInteractor {

    private final LocationRepository locationRepository;
    private final SearchRepository searchRepository;
    private Set<LocationClusterItem> selectedMapLocations;
    private LocationEntity selectedManualLocation;
    private MapPosition position;
    private String mapMode;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public LocationInteractor(LocationRepository locationRepository,
                              SearchRepository searchRepository) {
        this.locationRepository = locationRepository;
        mapMode = locationRepository.getMapMode();
        position = locationRepository.getMapPosition();
        this.searchRepository = searchRepository;
    }

    public String getMapMode() {
        return mapMode;
    }

    public void setMapMode(String mode) {
        mapMode = mode;
    }

    public MapPosition getPosition() {
        return position;
    }

    public void setMapPosition(MapPosition position) {
        this.position = position;
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
        locationRepository.saveMapPosition(position);
        return true;
    }

    public boolean saveManualSelection() {
        if (selectedManualLocation != null) {
            Set<String> ids = Collections.singleton(String.valueOf(selectedManualLocation.id));
            locationRepository.saveLocations(ids);
            return true;
        } else {
            return false;
        }
    }

    public Single<Set<LocationClusterItem>> getMapLocations() {
        return locationRepository.getSavedLocations()
                .filter(locationEntities -> searchRepository.getSearchMode() == SearchPresenter.MAP_MODE)
                .flattenAsObservable(locationEntities -> locationEntities)
                .map(LocationClusterItem::new)
                .<Set<LocationClusterItem>>collect(HashSet::new, Set::add)
                .doOnSuccess(locations -> selectedMapLocations = locations);
    }

    public Single<LocationEntity> getManualLocation() {
        return locationRepository.getSavedLocations()
                .filter(locationEntities -> !locationEntities.isEmpty()
                        && searchRepository.getSearchMode() == SearchPresenter.MANUAL_MODE)
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

    public Observable<LocationClusterItem> setMyLocation(MapPosition position) {
        switch (mapMode) {
            case MapWrapper.RADIUS_MODE:
                return Observable.empty();
            case MapWrapper.COUNTRY_MODE:
                return getCountryFromLocation(position);
            default:
                return getCityFromLocation(position);
        }
    }

    public boolean isServicesAvailable() {
        return locationRepository.isServicesAvailable();
    }

    private Observable<LocationClusterItem> getCountryFromLocation(MapPosition position) {
        Pair<String, String> countryCodeCity = locationRepository.getCountryCodeCity(position);
        return getCountry(countryCodeCity.first)
                .map(LocationClusterItem::new)
                .toObservable();
    }

    private Observable<LocationClusterItem> getCityFromLocation(MapPosition position) {
        Bounds bounds = Bounds.fromCenter(position.getLatLng(), 5);
        return loadClusters(MapUtils.createQueryFor(bounds, false))
                .flatMapObservable(locationClusterItems -> {
                    LocationClusterItem closest = MapUtils.closestToCenter(position.getLatLng(),
                            locationClusterItems);
                    return closest == null ? Observable.empty() : Observable.just(closest);
                });
    }
}
