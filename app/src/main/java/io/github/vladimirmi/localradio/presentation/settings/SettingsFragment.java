package io.github.vladimirmi.localradio.presentation.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.preferences.Preferences;
import io.github.vladimirmi.localradio.presentation.core.BaseSettingsFragment;

/**
 * Created by Vladimir Mikhalev 31.10.2018.
 */
public class SettingsFragment extends BaseSettingsFragment {


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(Preferences.NAME);
        addPreferencesFromResource(R.xml.settings_screen);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof SeekBarDialogPreference) {
            SeekBarDialogFragment fragment = SeekBarDialogFragment.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), "SeekBarDialogFragment");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }


}
