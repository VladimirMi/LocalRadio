package io.github.vladimirmi.localradio.data.preferences;

import android.content.SharedPreferences;

import timber.log.Timber;

/**
 * Generic class, which represents an entry for the {@link SharedPreferences},
 * encapsulates logic of retrieving and editing a value.
 */

public class Preference<T> {

    private final String key;
    private final T defaultValue;
    private final SharedPreferences prefs;

    public Preference(SharedPreferences prefs, String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefs = prefs;
    }

    @SuppressWarnings("unchecked")
    public T get() {
        PreferenceType type;
        try {
            type = PreferenceType.valueOf(defaultValue.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {
            Timber.e("Can not find type %s", defaultValue.getClass().getSimpleName());
            return defaultValue;
        }

        switch (type) {
            case String:
                return (T) prefs.getString(key, (String) defaultValue);
            case Long:
                return (T) Long.valueOf(prefs.getLong(key, (Long) defaultValue));
            case Float:
                return (T) Float.valueOf(prefs.getFloat(key, (Float) defaultValue));
            case Boolean:
                return (T) Boolean.valueOf(prefs.getBoolean(key, (Boolean) defaultValue));
            case Integer:
                return (T) Integer.valueOf(prefs.getInt(key, (Integer) defaultValue));
        }
        return defaultValue;
    }

    public void put(T value) {
        PreferenceType type;
        try {
            type = PreferenceType.valueOf(defaultValue.getClass().getSimpleName());
        } catch (IllegalArgumentException e) {
            Timber.e("Can not find type %s", defaultValue.getClass().getSimpleName());
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        switch (type) {
            case String:
                editor.putString(key, (String) value);
                break;
            case Long:
                editor.putLong(key, (Long) value);
                break;
            case Float:
                editor.putFloat(key, (Float) value);
                break;
            case Boolean:
                editor.putBoolean(key, (Boolean) value);
                break;
            case Integer:
                editor.putInt(key, (Integer) value);
                break;
        }
        editor.apply();
    }
}
