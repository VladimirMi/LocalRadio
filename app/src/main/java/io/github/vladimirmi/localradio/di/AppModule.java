package io.github.vladimirmi.localradio.di;

import android.content.Context;

import com.squareup.moshi.Moshi;

import io.github.vladimirmi.localradio.data.db.AppDatabase;
import io.github.vladimirmi.localradio.data.net.NetworkChecker;
import io.github.vladimirmi.localradio.data.net.RestService;
import io.github.vladimirmi.localradio.data.net.RestServiceProvider;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.data.repository.FavoriteRepository;
import io.github.vladimirmi.localradio.data.repository.LocationRepository;
import io.github.vladimirmi.localradio.data.repository.MediaController;
import io.github.vladimirmi.localradio.data.repository.StationsRepository;
import io.github.vladimirmi.localradio.data.source.CacheSource;
import io.github.vladimirmi.localradio.domain.interactors.FavoriteInteractor;
import io.github.vladimirmi.localradio.domain.interactors.LocationInteractor;
import io.github.vladimirmi.localradio.domain.interactors.MainInteractor;
import io.github.vladimirmi.localradio.domain.interactors.PlayerControlInteractor;
import io.github.vladimirmi.localradio.domain.interactors.SearchInteractor;
import io.github.vladimirmi.localradio.domain.interactors.StationsInteractor;
import io.github.vladimirmi.localradio.presentation.favorite.FavoritePresenter;
import io.github.vladimirmi.localradio.presentation.main.MainPresenter;
import io.github.vladimirmi.localradio.presentation.playercontrol.PlayerControlPresenter;
import io.github.vladimirmi.localradio.presentation.search.SearchPresenter;
import io.github.vladimirmi.localradio.presentation.stations.StationsPresenter;
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

        bind(Preferences.class).singletonInScope();

        bind(LocationRepository.class).singletonInScope();
        bind(StationsRepository.class).singletonInScope();
        bind(FavoriteRepository.class).singletonInScope();
        bind(MediaController.class).singletonInScope();

        bind(FavoriteInteractor.class).singletonInScope();
        bind(PlayerControlInteractor.class).singletonInScope();
        bind(SearchInteractor.class).singletonInScope();
        bind(LocationInteractor.class).singletonInScope();
        bind(StationsInteractor.class).singletonInScope();
        bind(MainInteractor.class).singletonInScope();

        bind(FavoritePresenter.class).singletonInScope();
        bind(PlayerControlPresenter.class).singletonInScope();
        bind(SearchPresenter.class).singletonInScope();
        bind(StationsPresenter.class).singletonInScope();
        bind(MainPresenter.class).singletonInScope();
    }
}
