package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.net.SocketTimeoutException;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.presentation.core.BaseView;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class UiUtils {

    private UiUtils() {
    }

    public static void handleError(@Nullable Object errorHandler, Throwable e) {
        int messageId = 0;
        if (e instanceof MessageException) {
            messageId = ((MessageException) e).getMessageId();

        } else if (e instanceof SocketTimeoutException) {
            messageId = R.string.error_connection;

        } else if (e instanceof ResolvableApiException && errorHandler instanceof BaseView) {
            ResolvableApiException resolvable = (ResolvableApiException) e;
            ((BaseView) errorHandler).resolveApiException(resolvable);

        } else if (e instanceof HttpException) {
            // TODO: 5/11/18 handle fail codes (500...)
            messageId = R.string.error_unexpected;
        } else {
            messageId = R.string.error_unexpected;
        }

        if (errorHandler != null && messageId != 0) {
            if (errorHandler instanceof BaseView) {
                ((BaseView) errorHandler).showMessage(messageId);
            } else if (errorHandler instanceof Context) {
                int finalMessageId = messageId;
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText((Context) errorHandler, finalMessageId, Toast.LENGTH_SHORT).show();
                });
            }
        } else {
            Timber.e(e);
        }
    }

    public static void setLinkStyle(TextView textView) {
        Context context = textView.getContext();
        String string = textView.getText().toString();
        int color = ContextCompat.getColor(context, R.color.blue_500);
        SpannableString spannable = new SpannableString(string);
        spannable.setSpan(new URLSpan(string), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }

    public static int spToPx(Context context, int sp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.scaledDensity * sp);
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.density * dp);
    }

    public static double dpToPx(Context context, double dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.density * dp;
    }

    public static void hideSoftKeyBoard(Context context, IBinder windowToken) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    public static void showSoftKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            view.requestFocus();
        }
    }
}
