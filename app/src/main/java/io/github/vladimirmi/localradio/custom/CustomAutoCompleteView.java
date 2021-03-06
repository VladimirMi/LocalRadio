package io.github.vladimirmi.localradio.custom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.utils.UiUtils;

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

        setOnDismissListener(() -> postDelayed(() -> isPopupDismissed = true, 100));

        clearButtonImage = ResourcesCompat.getDrawable(getResources(),
                R.drawable.ic_clear, null);

        addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if (!s.toString().isEmpty()) showClearButton();
            }
        });

        setOnTouchListener((view, motionEvent) -> {
            if ((getCompoundDrawables()[2] != null) && isClearButtonClicked(motionEvent)) {
                setText("", false);
                performValidation();
                performClick();
                UiUtils.showSoftKeyBoard(this);
                return true;
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
        super.performValidation();
        if (getText().toString().isEmpty()) {
            listener.onCompletion(null);
            hideClearButton();
        } else {
            //noinspection unchecked
            T item = (T) ((CustomArrayAdapter) getAdapter()).findItem(getText().toString());
            listener.onCompletion(item);
        }
    }

    private void showPopup() {
        showDropDown();
        isPopupDismissed = false;
    }

    private void showClearButton() {
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, clearButtonImage, null);
    }

    private void hideClearButton() {
        setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
    }

    private boolean isClearButtonClicked(MotionEvent motionEvent) {
        boolean isInBounds;
        if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
            float clearButtonEnd = clearButtonImage.getIntrinsicWidth() + getPaddingStart();
            isInBounds = motionEvent.getX() < clearButtonEnd;
        } else {
            float clearButtonStart = (getWidth() - getPaddingEnd()
                    - clearButtonImage.getIntrinsicWidth());
            isInBounds = motionEvent.getX() > clearButtonStart;
        }
        return isInBounds && motionEvent.getAction() == MotionEvent.ACTION_UP;
    }

    public interface OnCompletionListener<T> {

        void onCompletion(T item);
    }

}
