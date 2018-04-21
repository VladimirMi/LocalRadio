package io.github.vladimirmi.localradio.data.source;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by Vladimir Mikhalev 12.04.2018.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class CacheSource implements Interceptor {

    private static final String CACHE_PREFIX = "cache";
    private static final String SEARCH_PREFIX = "cache_search";
    private static final String URL_PREFIX = "cache_url";
    private static final String SUFFIX = "json";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.ENGLISH);

    private final File cacheDir;

    public CacheSource(Context context) {
        cacheDir = context.getCacheDir();
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        File cacheFile = createLocationSearchCache(chain.request().url());
        if (cacheFile == null) {
            cacheFile = createCoordinatesSearchCache(chain.request().url());
        }
        if (cacheFile == null) {
            cacheFile = createIpSearchCache(chain.request().url());
        }
        if (cacheFile == null) {
            cacheFile = createUrlQueryCache(chain.request().url());
        }
        if (cacheFile == null) {
            return chain.proceed(chain.request());
        }

        if (!cacheFile.exists()) {
            cleanOldCache();

            Response response = chain.proceed(chain.request());
            if (response.isSuccessful()) {
                FileWriter fileWriter = new FileWriter(cacheFile);
                fileWriter.write(response.body().string());
                fileWriter.close();
            } else {
                return response;
            }
        }

        BufferedSource cache = Okio.buffer(FileSystem.SYSTEM.source(cacheFile));

        return new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(new RealResponseBody("text", cacheFile.length(), cache))
                .build();
    }


    public void cleanCache(String namePart) {
        File[] files = cacheDir.listFiles((dir, name) -> name.contains(namePart));
        for (File file : files) {
            file.delete();
        }
    }

    private void cleanOldCache() {
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith(CACHE_PREFIX));
        String nowDate = dateFormat.format(new Date());
        for (File file : files) {
            if (!file.getName().contains(nowDate)) {
                file.delete();
            }
        }
    }

    private File createLocationSearchCache(HttpUrl url) {
        String country = url.queryParameter("country");
        String city = url.queryParameter("city");

        if (country == null || city == null) {
            return null;
        }
        String fileName = String.format("%s_%s_%s_%s.%s", SEARCH_PREFIX, dateFormat.format(new Date()),
                country, city, SUFFIX);

        return new File(cacheDir, fileName);
    }

    private File createCoordinatesSearchCache(HttpUrl url) {
        String latitude = url.queryParameter("latitude");
        String longitude = url.queryParameter("longitude");

        if (latitude == null || longitude == null) {
            return null;
        }
        String fileName = String.format("%s_%s_%s_%s.%s", SEARCH_PREFIX, dateFormat.format(new Date()),
                latitude, longitude, SUFFIX);

        return new File(cacheDir, fileName);
    }

    private File createIpSearchCache(HttpUrl url) {
        String ip = url.queryParameter("ip");

        if (ip == null) return null;
        String fileName = String.format("%s_%s_%s.%s", SEARCH_PREFIX, dateFormat.format(new Date()), ip, SUFFIX);

        return new File(cacheDir, fileName);
    }

    private File createUrlQueryCache(HttpUrl url) {
        List<String> pathSegments = url.pathSegments();
        String id = url.queryParameter("station_id");

        if (id == null || !pathSegments.get(pathSegments.size() - 1).equals("uberstationurl.php")) {
            return null;
        }

        String fileName = String.format("%s_%s_%s.%s", URL_PREFIX, dateFormat.format(new Date()), id, SUFFIX);

        return new File(cacheDir, fileName);
    }
}
