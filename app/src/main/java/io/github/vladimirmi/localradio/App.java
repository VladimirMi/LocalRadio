package io.github.vladimirmi.localradio;

import android.app.Application;

import com.facebook.stetho.Stetho;

import timber.log.Timber;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

/**
 * Class for maintaining global application state.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if (BuildConfig.DEBUG) {
            Toothpick.setConfiguration(Configuration.forDevelopment().preventMultipleRootScopes());
        } else {
            Toothpick.setConfiguration(Configuration.forProduction().disableReflection());
            FactoryRegistryLocator.setRootRegistry(new FactoryRegistry());
            MemberInjectorRegistryLocator.setRootRegistry(new MemberInjectorRegistry());
        }
    }
}
