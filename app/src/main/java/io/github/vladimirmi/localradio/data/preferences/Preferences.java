package io.github.vladimirmi.localradio.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String KEY_AUTODETECT = "AUTODETECT";
    public final Preference<Boolean> autodetectPref;

    public Preferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("default", Context.MODE_PRIVATE);

        autodetectPref = new Preference<>(prefs, KEY_AUTODETECT, false);
    }
}
