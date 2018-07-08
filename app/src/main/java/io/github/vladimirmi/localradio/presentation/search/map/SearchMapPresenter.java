package io.github.vladimirmi.localradio.presentation.search.map;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.data.db.location.LocationEntity;
import io.github.vladimirmi.localradio.domain.models.LocationCluster;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;
import io.reactivex.disposables.CompositeDisposable;

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

    @Override
    protected void onFirstAttach(SearchMapView view, CompositeDisposable disposables) {
        initOptions();
        initMapMode();
    }

    public void initOptions() {
        view.initOptions(locationRepository.getMapMode());
    }

    public void onMapReady() {
        List<LocationEntity> locations = locationRepository.getLocations();
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

    private void initMapMode() {
        switch (locationRepository.getMapMode()) {
            case EXACT_MODE:
                view.setExactMode();
                break;
            case RADIUS_MODE:
                view.setRadiusMode();
                break;
            case COUNTRY_MODE:
                view.setCountryMode();
        }
    }
}
