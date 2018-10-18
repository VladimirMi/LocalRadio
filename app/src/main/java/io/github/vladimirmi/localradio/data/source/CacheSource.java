package io.github.vladimirmi.localradio.data.source;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import io.github.vladimirmi.localradio.data.net.Api;
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
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 3;  // 3 days
    private final File cacheDir;

    public CacheSource(Context context) {
        cacheDir = context.getCacheDir();
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        cleanOldCache();

        File cacheFile = createCacheFile(chain.request().url());
        if (cacheFile == null) {
            return chain.proceed(chain.request());
        }

        if (!cacheFile.exists() || cacheFile.length() == 0) {
            Response response = chain.proceed(chain.request());
            if (response.isSuccessful()) {
                Timber.i("Write %s", cacheFile.getName());
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

    public void cleanCache(String... parts) {
        String partName = buildFileName(parts);
        File[] files = cacheDir.listFiles((dir, name) -> name.contains(partName));
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

    private @Nullable
    File createCacheFile(HttpUrl url) {
        if (!url.host().equals(Api.HOST)) {
            return null;
        }
        Set<String> names = url.queryParameterNames();
        String[] values = new String[names.size()];

        int i = 0;
        for (String name : names) {
            values[i] = url.queryParameter(name);
            i++;
        }
        String partName = buildFileName(values);
        File[] files = cacheDir.listFiles((dir, name) -> name.contains(partName));
        if (files.length > 0) {
            return files[0];
        }

        String fileName = String.format("%s_%s_%s.%s", PREFIX, partName, System.currentTimeMillis(), EXTENSION);
        return new File(cacheDir, fileName);
    }

    private String buildFileName(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {

            String part = parts[i].replaceAll("([ \\\\/])", "");
            builder.append(part);
            if (i != parts.length - 1) builder.append('_');
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
            Timber.w("Can't delete %s", file.getName());
        } else {
            Timber.w("Delete %s", file.getName());
        }
    }
}
