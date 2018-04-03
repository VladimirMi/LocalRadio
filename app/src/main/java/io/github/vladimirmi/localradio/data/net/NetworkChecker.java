package io.github.vladimirmi.localradio.data.net;

import android.content.Context;
import android.net.ConnectivityManager;


public class NetworkChecker {

    private final Context mContext;

    public NetworkChecker(Context context) {
        mContext = context;
    }

    public boolean isAvailableNet() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm != null && cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnected();
    }
}
