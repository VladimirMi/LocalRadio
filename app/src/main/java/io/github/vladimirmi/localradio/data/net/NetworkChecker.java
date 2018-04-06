package io.github.vladimirmi.localradio.data.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.WorkerThread;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class NetworkChecker {

    private final Context context;
    private final OkHttpClient client;
    public static final String API_CHECK = "https://api.ipify.org/";


    public NetworkChecker(Context context, OkHttpClient client) {
        this.context = context;
        this.client = client;
    }

    public boolean isAvailableNet() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnected();
    }

    @WorkerThread
    public String getIp() throws IOException {
        Request request = new Request.Builder()
                .url(API_CHECK)
                .build();
        Response response = client.newCall(request).execute();

        return response.body().string();
    }
}
