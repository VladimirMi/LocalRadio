package io.github.vladimirmi.localradio.di;

import android.content.Context;

import toothpick.Scope;
import toothpick.Toothpick;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public class Scopes {

    public static final String APP_SCOPE = "APP_SCOPE";
    public static final String LOCATIONS_SCOPE = "LOCATIONS_SCOPE";

    private Scopes() {
    }

    public static Scope getAppScope() {
        return Toothpick.openScope(APP_SCOPE);
    }

    public static Scope getLocationsScope() {
        return Toothpick.openScopes(APP_SCOPE, LOCATIONS_SCOPE);
    }

    public static Context appContext() {
        return getAppScope().getInstance(Context.class);
    }
}
