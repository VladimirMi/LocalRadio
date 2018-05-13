package io.github.vladimirmi.localradio.data.source;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSource;
import okio.Okio;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 12.04.2018.
 */
public class CacheSource implements Interceptor {

    private static final String PREFIX = "cache";
    private static final String EXTENSION = "json";
    private final File cacheDir;
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;  // 24 hours

    public CacheSource(Context context) {
        cacheDir = context.getCacheDir();
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        File cacheFile = createCacheFile(chain.request().url());

        if (!cacheFile.exists() || cacheFile.length() == 0) {
            cleanOldCache();

            Response response = chain.proceed(chain.request());
            if (response.isSuccessful()) {
                Timber.w("Write %s", cacheFile.getName());
                FileWriter fileWriter = new FileWriter(cacheFile);
                //noinspection ConstantConditions
                fileWriter.write(response.body().string());
                fileWriter.close();
            } else {
                return response;
            }
        } else {
            Timber.w("Return %s", cacheFile.getName());
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

    public void cleanCache(String... queries) {
        String query = buildQuery(queries);
        Timber.e("cleanCache: " + query);
        File[] files = cacheDir.listFiles((dir, name) -> name.contains(query));
        for (File file : files) {
            deleteFile(file);
        }
    }

    private void cleanOldCache() {
        File[] files = cacheDir.listFiles((dir, name) -> name.startsWith(PREFIX));
        for (File file : files) {
            if (isCacheExpired(file)) {
                deleteFile(file);
            }
        }
    }

    private File createCacheFile(HttpUrl url) {
        Set<String> names = url.queryParameterNames();
        String[] values = new String[names.size()];

        int i = 0;
        for (String name : names) {
            values[i] = url.queryParameter(name);
            i++;
        }
        String query = buildQuery(values);
        String fileName = String.format("%s_%s_%s.%s", PREFIX, query, System.currentTimeMillis(), EXTENSION);
        return new File(cacheDir, fileName);
    }

    private String buildQuery(String... queries) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < queries.length; i++) {
            builder.append(queries[i]);
            if (i != queries.length - 1) builder.append('_');
        }
        return builder.toString();
    }

    private boolean isCacheExpired(File cache) {
        int begin = cache.getName().lastIndexOf('_') + 1;
        int end = cache.getName().lastIndexOf('.');
        String createdTime = cache.getName().substring(begin, end);
        return Long.parseLong(createdTime) + EXPIRATION_TIME < System.currentTimeMillis();
    }

    private void deleteFile(File file) {
        boolean delete = file.delete();
        if (!delete) {
            Timber.i("Can't delete %s", file.getName());
        } else {
            Timber.i("Delete %s", file.getName());
        }
    }
}
