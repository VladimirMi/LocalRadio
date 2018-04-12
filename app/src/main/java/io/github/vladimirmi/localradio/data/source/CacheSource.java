package io.github.vladimirmi.localradio.data.source;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class CacheSource implements Interceptor {

    private static final String PREFIX = "query_cache";
    private static final String SUFFIX = ".json";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.ENGLISH);

    private final File cacheDir;

    public CacheSource(Context context) {
        cacheDir = context.getCacheDir();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        File cacheFile = createLocationQueryCache(chain.request().url());
        if (cacheFile == null) {
            cacheFile = createCoordinatesQueryCache(chain.request().url());
        }
        if (cacheFile == null) {
            cacheFile = createIpQueryCache(chain.request().url());
        }
        if (cacheFile == null) {
            return chain.proceed(chain.request());
        }

        if (!cacheFile.exists()) {
            cleanCache();

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

    public void cleanCache() {
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith(PREFIX));
        for (File file : files) {
            file.delete();
        }
    }

    private File createLocationQueryCache(HttpUrl url) {
        String country = url.queryParameter("country");
        String city = url.queryParameter("city");

        if (country == null || city == null) {
            return null;
        }
        String fileName = String.format("%s_%s_%s_%s_%s", PREFIX, country,
                city, dateFormat.format(new Date()), SUFFIX);

        return new File(cacheDir, fileName);
    }

    private File createCoordinatesQueryCache(HttpUrl url) {
        String latitude = url.queryParameter("latitude");
        String longitude = url.queryParameter("longitude");

        if (latitude == null || longitude == null) {
            return null;
        }
        String fileName = String.format("%s_%s_%s_%s_%s", PREFIX, latitude,
                longitude, dateFormat.format(new Date()), SUFFIX);

        return new File(cacheDir, fileName);
    }

    private File createIpQueryCache(HttpUrl url) {
        String ip = url.queryParameter("ip");

        if (ip == null) return null;
        String fileName = String.format("%s_%s_%s_%s", PREFIX, ip, dateFormat.format(new Date()), SUFFIX);

        return new File(cacheDir, fileName);
    }
}
