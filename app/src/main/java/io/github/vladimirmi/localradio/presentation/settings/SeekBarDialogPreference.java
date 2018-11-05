package io.github.vladimirmi.localradio.presentation.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;
import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 30.10.2018.
 */
public class SeekBarDialogPreference extends DialogPreference {

    private int progress = 0;

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SeekBarDialogPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        progress = getPersistedInt(progress);
        setSummary(createSummary(progress));
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        persistInt(progress);
        setSummary(createSummary(progress));
    }

    @Override
    protected Integer onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        int value;
        if (defaultValue == null) value = getPersistedInt(0);
        else value = (int) defaultValue;

        setProgress(value);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_seekbar;
    }

    public String createSummary(int progress) {
        return getContext().getResources().getQuantityString(R.plurals.plural_second, progress, progress);
    }
}
