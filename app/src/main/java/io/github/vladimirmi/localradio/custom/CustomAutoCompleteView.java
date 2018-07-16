package io.github.vladimirmi.localradio.custom;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.ListPopupWindow;

import java.lang.reflect.Field;

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

        setOnClickListener(v -> {
            if (isPopupDismissed) showPopup();
        });

        setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showPopup();
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

}
