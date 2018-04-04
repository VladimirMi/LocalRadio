package io.github.vladimirmi.localradio.data.net;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Provider for {@link RestService}.
 */

public class RestServiceProvider {

    private RestServiceProvider() {
    }

    public static RestService getService(Converter.Factory factory) {
        return createRetrofit(createClient(), factory).create(RestService.class);
    }

    private static OkHttpClient createClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .addNetworkInterceptor(new StethoInterceptor())
//                .addInterceptor(getApiKeyInterceptor())
                .connectTimeout(Api.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(Api.READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(Api.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    private static Retrofit createRetrofit(OkHttpClient okHttp, Converter.Factory factory) {
        return new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(factory)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(okHttp)
                .build();
    }

//    private static Interceptor getApiKeyInterceptor() {
//        return chain -> {
//            Request originalRequest = chain.request();
//
//            HttpUrl url = originalRequest.url().newBuilder()
//                    .addQueryParameter("api_key", BuildConfig.API_KEY)
//                    .build();
//
//            Request request = originalRequest.newBuilder()
//                    .url(url)
//                    .build();
//            return chain.proceed(request);
//        };
//    }
}
