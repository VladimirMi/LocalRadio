package io.github.vladimirmi.localradio.presentation.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;
import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 30.10.2018.
 */
public class SeekBarDialogFragment extends PreferenceDialogFragmentCompat {

    private SeekBar seekBar;
    private TextView seekBarValue;

    public static SeekBarDialogFragment newInstance(String key) {
        SeekBarDialogFragment fragment = new SeekBarDialogFragment();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        seekBar = view.findViewById(R.id.seekBar);
        seekBarValue = view.findViewById(R.id.valueTv);


        seekBar.setProgress(((SeekBarDialogPreference) getPreference()).getProgress());
        seekBarValue.setText(getPreference().getSummary());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarValue.setText(((SeekBarDialogPreference) getPreference()).createSummary(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult && getPreference().callChangeListener(seekBar.getProgress())) {
            ((SeekBarDialogPreference) getPreference()).setProgress(seekBar.getProgress());
        }
    }
}
