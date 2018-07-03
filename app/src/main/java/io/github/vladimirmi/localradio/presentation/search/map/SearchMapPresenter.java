package io.github.vladimirmi.localradio.presentation.search.map;

import javax.inject.Inject;

import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.presentation.core.BasePresenter;

/**
 * Created by Vladimir Mikhalev 02.07.2018.
 */
public class SearchMapPresenter extends BasePresenter<SearchMapView> {

    // TODO: 7/3/18 interactor
    private final LocationRepository locationRepository;

    @Inject
    public SearchMapPresenter(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public void onMapReady() {

    }
}
