package io.github.vladimirmi.localradio.presentation.search.map;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapPresenter extends BasePresenter<SearchMapView> {

    public static final String EXACT_MODE = "EXACT_MODE";
    public static final String RADIUS_MODE = "RADIUS_MODE";
    public static final String COUNTRY_MODE = "COUNTRY_MODE";

    // TODO: 7/3/18 interactor
    private final LocationRepository locationRepository;

    @Inject
    public SearchMapPresenter(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void onMapReady() {
        List<LocationEntity> locations = locationRepository.getLocations();
        Timber.e("onMapReady: " + locations.size());
        List<LocationCluster> clusters = new ArrayList<>(locations.size());

        for (LocationEntity location : locations) {
            clusters.add(new LocationCluster(location));
        }

        view.setClusterItems(clusters);
    }

    public void selectCountry() {
        locationRepository.saveMapMode(COUNTRY_MODE);
    }

    public void selectRadius() {
        locationRepository.saveMapMode(RADIUS_MODE);
    }

    public void selectExact() {
        locationRepository.saveMapMode(EXACT_MODE);
    }
}
