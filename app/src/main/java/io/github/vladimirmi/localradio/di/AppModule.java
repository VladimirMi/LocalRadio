package io.github.vladimirmi.localradio.di;

import android.content.Context;

import com.squareup.moshi.Moshi;

import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RestServiceProvider;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.repository.GeoLocationRepository;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import okhttp3.OkHttpClient;
import retrofit2.converter.moshi.MoshiConverterFactory;
import toothpick.config.Module;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public class AppModule extends Module {

    public AppModule(Context context) {

        bind(Context.class).toInstance(context);

        Moshi moshi = new Moshi.Builder().build();
        OkHttpClient client = RestServiceProvider.createClient();

        bind(Moshi.class).toInstance(moshi);
        bind(RestService.class).toInstance(RestServiceProvider.getService(client,
                MoshiConverterFactory.create(moshi)));
        bind(NetworkChecker.class).toInstance(new NetworkChecker(context, client));

        bind(Preferences.class).toInstance(new Preferences(context));

        bind(GeoLocationRepository.class).singletonInScope();
        bind(StationsRepository.class).singletonInScope();
    }
}
