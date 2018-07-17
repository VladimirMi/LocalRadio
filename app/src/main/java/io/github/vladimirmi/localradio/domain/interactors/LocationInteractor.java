package io.github.vladimirmi.localradio.domain.interactors;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Vladimir Mikhalev 24.04.2018.
 */
public class LocationInteractor {

    private final LocationRepository locationRepository;

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

    public void saveLocations(int... locationId) {
        locationRepository.saveLocations(locationId);
    }

    public Observable<List<LocationEntity>> getSavedLocations() {
        return locationRepository.getSavedLocations();
    }


    public Single<List<LocationCluster>> getCityClusters() {
        return locationRepository.getCities("")
                .toObservable()
                .flatMapIterable(locationEntities -> locationEntities)
                .map(LocationCluster::new)
                .toList();
    }

    public Single<List<LocationCluster>> getCountryClusters() {
        return locationRepository.getCountries()
                .flattenAsObservable(locationEntities -> locationEntities)
                .map(LocationCluster::new)
                .toList();
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
}
