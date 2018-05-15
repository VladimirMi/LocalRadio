package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ListPopupWindow;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Vladimir Mikhalev 05.04.2018.
 */

public class CustomAutoCompleteView extends AppCompatAutoCompleteTextView {

    private boolean isPopupDismissed = true;
    private OnCompletionListener listener;
    private EditTextLabelView label;

    public CustomAutoCompleteView(Context context) {
        this(context, null);
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        this(context, attrs, android.support.v7.appcompat.R.attr.autoCompleteTextViewStyle);
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOnClickListener(v -> {
            if (isPopupDismissed) showPopup();
        });

        setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showPopup();
            if (label != null) label.setFocused(hasFocus);
        });

        trySetOnDismissListener();
    }

    private void trySetOnDismissListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Class<?> parent = AutoCompleteTextView.class;
                //noinspection JavaReflectionMemberAccess
                Field popupField = parent.getDeclaredField("mPopup");
                popupField.setAccessible(true);
                ListPopupWindow popup = (ListPopupWindow) popupField.get(this);
                popup.setOnDismissListener(() -> postDelayed(() -> isPopupDismissed = true, 100));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setOnDismissListener(() -> postDelayed(() -> isPopupDismissed = true, 100));
        }
    }

    private void showPopup() {
        showDropDown();
        isPopupDismissed = false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        label = findLabel();
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
        if (getText().toString().isEmpty()) setText(" ");
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
            if (text.toString().equals(" ")) return false;
            for (T element : list) {
                if (element.toString().equals(text.toString())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            if (invalidText.toString().trim().isEmpty()) return "";
            int maxCharEquals = 0;
            int elementIndex = 0;

            for (int idx = 0; idx < list.size(); idx++) {
                String element = list.get(idx).toString();
                int charEquals = 0;

                int minLength = Math.min(element.length(), invalidText.length());
                for (int i = 0; i < minLength; i++) {
                    char actual = invalidText.charAt(i);
                    char expected = element.charAt(i);

                    if (actual == expected) {
                        charEquals++;
                        if (charEquals == minLength) {
                            return element;
                        }
                    }
                    if (charEquals > maxCharEquals) {
                        maxCharEquals = charEquals;
                        elementIndex = idx;
                    }
                }
            }

            return list.isEmpty() ? "" : list.get(elementIndex).toString();
        }
    }

    private EditTextLabelView findLabel() {
        ViewGroup parent = (ViewGroup) getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (Build.VERSION.SDK_INT >= 17 && parent.getChildAt(i).getLabelFor() == getId()
                    || Build.VERSION.SDK_INT < 17 && parent.getChildAt(i) instanceof EditTextLabelView) {
                return (EditTextLabelView) parent.getChildAt(i);
            }
        }
        return null;
    }
}
