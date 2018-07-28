package io.github.vladimirmi.localradio.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;
import android.widget.ListPopupWindow;

import java.lang.reflect.Field;

import io.github.vladimirmi.localradio.R;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 05.04.2018.
 */

public class CustomAutoCompleteView<T> extends AppCompatAutoCompleteTextView {

    private Drawable clearButtonImage;
    private boolean isPopupDismissed = true;
    private OnCompletionListener<T> listener;

    public CustomAutoCompleteView(Context context) {
        super(context);
        init();
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnClickListener(v -> {
            if (isPopupDismissed) showPopup();
        });

        setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showPopup();
        });

        trySetOnDismissListener();

        clearButtonImage = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_clear, null);

        addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                showClearButton();
            }
        });

        setOnTouchListener((view, motionEvent) -> {
            performClick();
            if ((getCompoundDrawables()[2] != null)) {
                float clearButtonStart;
                boolean isClearButtonClicked = false;
                clearButtonStart = (getWidth() - getPaddingRight()
                        - clearButtonImage.getIntrinsicWidth());
                if (motionEvent.getX() > clearButtonStart) {
                    isClearButtonClicked = true;
                }
                if (isClearButtonClicked) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        clearButtonImage = ResourcesCompat.getDrawable(getResources(),
                                R.drawable.ic_clear, null);
                        getText().clear();
                        hideClearButton();
                        return true;
                    }
                } else {
                    return false;
                }
            }
            return false;
        });
    }


    public void setOnCompletionListener(OnCompletionListener<T> listener) {
        this.listener = listener;
        //noinspection unchecked
        setOnItemClickListener((parent, v, position, id) ->
                listener.onCompletion((T) parent.getItemAtPosition(position)));
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }

    @Override
    public void performValidation() {
        // TODO: 7/16/18 ??
        if (getText().toString().isEmpty()) setText(" ");
        super.performValidation();

        //noinspection unchecked
        T item = (T) ((CustomArrayAdapter) getAdapter()).findItem(getText().toString());
        listener.onCompletion(item);
    }

    private void showPopup() {
        showDropDown();
        isPopupDismissed = false;
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

    private void showClearButton() {
        Timber.e("showClearButton: ");
        setCompoundDrawablesWithIntrinsicBounds(null, null, clearButtonImage, null);
    }


    private void hideClearButton() {
        setCompoundDrawables(null, null, null, null);
    }

    public interface OnCompletionListener<T> {

        void onCompletion(T item);
    }

}
