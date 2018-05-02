package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.presentation.core.BaseView;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */

public class UiUtils {

    private UiUtils() {
    }

    public static void handleError(Object errorHandler, Throwable e) {
        int messageId = -1;
        if (e instanceof MessageException) {
            messageId = ((MessageException) e).getMessageId();
        } else if (e instanceof SocketTimeoutException) {
            messageId = R.string.error_network;
        }

        if (errorHandler != null && messageId != -1) {
            if (errorHandler instanceof BaseView) {
                ((BaseView) errorHandler).showMessage(messageId);
            } else if (errorHandler instanceof Context) {
                Toast.makeText((Context) errorHandler, messageId, Toast.LENGTH_SHORT).show();
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

    public static Bitmap loadBitmap(Context context, String imageUrl) {
        Resources resources = context.getResources();
        int width = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int height = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
        Timber.e("loadBitmap: " + imageUrl);

        FutureTarget<Bitmap> futureTarget = Glide.with(context.getApplicationContext())
                .load(imageUrl)
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .error(R.drawable.ic_radio)
                .into(width, height);

        try {
            return futureTarget.get(3000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Timber.w("error loading notification icon");
        } finally {
            Glide.clear(futureTarget);
        }
        return null;
    }
}
