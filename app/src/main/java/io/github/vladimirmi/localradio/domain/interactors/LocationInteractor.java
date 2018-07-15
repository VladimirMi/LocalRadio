package io.github.vladimirmi.localradio.domain.interactors;

import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.utils.MessageException;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

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

    public Observable<List<LocationEntity>> getCountries() {
        return locationRepository.getCountries()
                .subscribeOn(Schedulers.io());
    }

    public Observable<List<LocationEntity>> getCities(String country) {
        return locationRepository.getCities(country)
                .subscribeOn(Schedulers.io());
    }

    public void saveLocations(int... locationId) {
        locationRepository.saveLocations(locationId);
    }

    public Observable<List<LocationEntity>> getSavedLocations() {
        return locationRepository.getSavedLocations()
                .subscribeOn(Schedulers.io());
    }


    public Single<List<LocationCluster>> getCityClusters() {
        return locationRepository.getCities("")
                .flatMapIterable(locationEntities -> locationEntities)
                .map(LocationCluster::new)
                .toList()
                .observeOn(Schedulers.io());
    }

    public Single<List<LocationCluster>> getCountryClusters() {
        return locationRepository.getCountries()
                .flatMapIterable(locationEntities -> locationEntities)
                .map(LocationCluster::new)
                .toList()
                .observeOn(Schedulers.io());
    }

    public Completable checkCanSearch() {
        if (getCountryName().isEmpty() && getCity().isEmpty()) {
            return Completable.error(new MessageException(R.string.error_specify_location));
        } else {
            return Completable.complete();
        }
    }

    public boolean isServicesAvailable() {
        return locationRepository.isServicesAvailable();
    }

}
