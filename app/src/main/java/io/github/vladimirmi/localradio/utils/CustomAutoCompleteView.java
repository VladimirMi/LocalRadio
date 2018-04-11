package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;

import java.util.List;

/**
 * Created by Vladimir Mikhalev 05.04.2018.
 */

public class CustomAutoCompleteView extends AppCompatAutoCompleteTextView {

    private boolean isPopupDismissed = true;

    public CustomAutoCompleteView(Context context) {
        this(context, null);
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.autoCompleteTextViewStyle);
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.setOnClickListener(v -> {
            if (isPopupDismissed) showPopup();
        });

        this.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showPopup();
        });

        setOnDismissListener(() -> postDelayed(() -> isPopupDismissed = true, 100));

        setValidator(new Validator() {
            @Override
            public boolean isValid(CharSequence text) {
                return false;
            }

            @Override
            public CharSequence fixText(CharSequence invalidText) {
                return null;
            }
        });
    }

    private void showPopup() {
        showDropDown();
        isPopupDismissed = false;
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    public static class CustomValidator<T> implements Validator {

        private final List<T> list;

        public CustomValidator(List<T> list) {
            this.list = list;
        }

        @Override
        public boolean isValid(CharSequence text) {
            for (T element : list) {
                if (element.toString().equals(text.toString())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            return list.get(0).toString();
        }
    }
}
