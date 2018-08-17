package io.github.vladimirmi.localradio.di;

import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import toothpick.config.Module;

/**
 * Created by Vladimir Mikhalev 08.08.2018.
 */
public class LocationsModule extends Module {

    public LocationsModule() {

        bind(LocationInteractor.class).singletonInScope();
    }
}
