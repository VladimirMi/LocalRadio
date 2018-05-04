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
    private OnCompletionListener listener;

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
    }

    private void showPopup() {
        showDropDown();
        isPopupDismissed = false;
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }


    public void setOnCompletionListener(OnCompletionListener listener) {
        this.listener = listener;
        setOnItemClickListener((parent, v, position, id) ->
                listener.onCompletion((String) parent.getItemAtPosition(position)));
    }

    @Override
    public void performValidation() {
        super.performValidation();
        listener.onCompletion(getText().toString());
    }

    public interface OnCompletionListener {

        void onCompletion(String text);
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
            int maxCharEquals = 0;
            int elementIndex = 0;

            for (int idx = 0; idx < list.size(); idx++) {
                String element = list.get(idx).toString();
                int charEquals = 0;

                int minLenght = Math.min(element.length(), invalidText.length());
                for (int i = 0; i < minLenght; i++) {
                    char actual = invalidText.charAt(i);
                    char expected = element.charAt(i);

                    if (actual == expected) {
                        charEquals++;
                        if (charEquals == minLenght) {
                            return element;
                        }
                    }
                    if (charEquals > maxCharEquals) {
                        maxCharEquals = charEquals;
                        elementIndex = idx;
                    }
                }
            }

            return list.get(elementIndex).toString();
        }
    }
}
