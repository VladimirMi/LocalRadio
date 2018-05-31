package io.github.vladimirmi.localradio.data;

import android.content.Context;

import javax.inject.Inject;

/**
 * Created by Vladimir Mikhalev 31.05.2018.
 */
public class ResourceManager {

    private final Context appContext;

    @SuppressWarnings("WeakerAccess")
    @Inject
    public ResourceManager(Context appContext) {
        this.appContext = appContext;
    }

    public String getString(int resId) {
        return appContext.getString(resId);
    }

    public String getFormatString(int resId, Object... formatArgs) {
        return appContext.getString(resId, formatArgs);
    }

    public String getQuantityString(int resId, int quantity) {
        return appContext.getResources().getQuantityString(resId, quantity, quantity);
    }
}
