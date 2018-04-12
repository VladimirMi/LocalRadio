package io.github.vladimirmi.localradio.di;

import android.content.Context;

import com.squareup.moshi.Moshi;

import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RestServiceProvider;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.repository.GeoLocationRepository;
import io.github.vladimirmi.localradio.data.repository.MediaController;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.data.source.CacheSource;
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
        MoshiConverterFactory factory = MoshiConverterFactory.create(moshi);
        CacheSource cacheSource = new CacheSource(context);
        OkHttpClient client = RestServiceProvider.createClient(cacheSource);

        bind(Moshi.class).toInstance(moshi);
        bind(OkHttpClient.class).toInstance(client);
        bind(CacheSource.class).toInstance(cacheSource);

        bind(RestService.class).toInstance(RestServiceProvider.getService(client, factory));
        bind(NetworkChecker.class).singletonInScope();

        bind(Preferences.class).singletonInScope();

        bind(GeoLocationRepository.class).singletonInScope();
        bind(StationsRepository.class).singletonInScope();
        bind(MediaController.class).singletonInScope();
    }
}
