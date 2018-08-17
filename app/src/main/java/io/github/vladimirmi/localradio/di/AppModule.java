package io.github.vladimirmi.localradio.di;

import android.content.Context;

import com.squareup.moshi.Moshi;

import io.github.vladimirmi.localradio.data.db.favorite.AppDatabase;
import io.github.vladimirmi.localradio.data.db.location.LocationDatabase;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RestServiceProvider;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.repositories.FavoriteRepositoryImpl;
import io.github.vladimirmi.localradio.data.repositories.LocationRepositoryImpl;
import io.github.vladimirmi.localradio.data.repositories.PlayerControllerImpl;
import io.github.vladimirmi.localradio.data.repositories.SearchRepositoryImpl;
import io.github.vladimirmi.localradio.data.repositories.StationsRepositoryImpl;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.domain.interactors.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.interactors.MainInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlsInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.domain.repositories.FavoriteRepository;
import io.github.vladimirmi.localradio.domain.repositories.LocationRepository;
import io.github.vladimirmi.localradio.domain.repositories.PlayerController;
import io.github.vladimirmi.localradio.domain.repositories.SearchRepository;
import io.github.vladimirmi.localradio.domain.repositories.StationsRepository;
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

        bind(AppDatabase.class).toInstance(AppDatabase.getInstance(context));
        bind(LocationDatabase.class).toInstance(LocationDatabase.getInstance(context));

        bind(Preferences.class).singletonInScope();

        bind(LocationRepository.class).to(LocationRepositoryImpl.class).singletonInScope();
        bind(StationsRepository.class).to(StationsRepositoryImpl.class).singletonInScope();
        bind(SearchRepository.class).to(SearchRepositoryImpl.class).singletonInScope();
        bind(FavoriteRepository.class).to(FavoriteRepositoryImpl.class).singletonInScope();
        bind(PlayerController.class).to(PlayerControllerImpl.class).singletonInScope();

        bind(FavoriteInteractor.class).singletonInScope();
        bind(PlayerControlsInteractor.class).singletonInScope();
        bind(SearchInteractor.class).singletonInScope();
        bind(StationsInteractor.class).singletonInScope();
        bind(MainInteractor.class).singletonInScope();
    }
}
