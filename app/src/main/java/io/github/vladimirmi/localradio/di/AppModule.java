package io.github.vladimirmi.localradio.di;

import android.content.Context;

import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RestServiceProvider;
import toothpick.config.Module;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public class AppModule extends Module {

    public AppModule(Context context) {

        bind(RestService.class).toInstance(RestServiceProvider.getService());
        bind(NetworkChecker.class).toInstance(new NetworkChecker(context));
    }
}
