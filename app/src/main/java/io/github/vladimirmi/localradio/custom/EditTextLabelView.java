package io.github.vladimirmi.localradio.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 15.05.2018.
 */
public class EditTextLabelView extends android.support.v7.widget.AppCompatTextView {

    private final int defaultColor = ContextCompat.getColor(getContext(), R.color.grey_600);
    private final int focusedColor = ContextCompat.getColor(getContext(), R.color.colorAccent);

    public EditTextLabelView(Context context) {
        this(context, null);
    }

    public EditTextLabelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditTextLabelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFocused(boolean focused) {
        setTextColor(focused ? focusedColor : defaultColor);
    }
}
