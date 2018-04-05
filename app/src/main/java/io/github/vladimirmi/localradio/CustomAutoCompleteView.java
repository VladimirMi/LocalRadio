package io.github.vladimirmi.localradio;

import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;

/**
 * Created by Vladimir Mikhalev 05.04.2018.
 */

public class CustomAutoCompleteView extends AppCompatAutoCompleteTextView {

    private boolean isPopupDissmissed = true;

    public CustomAutoCompleteView(Context context) {
        this(context, null);
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.autoCompleteTextViewStyle);
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setOnClickListener(v -> {
            if (isPopupDissmissed) showPopup();
        });

        this.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showPopup();
        });

        setOnDismissListener(() -> postDelayed(() -> isPopupDissmissed = true, 100));
    }

    private void showPopup() {
        showDropDown();
        isPopupDissmissed = false;
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }
}
