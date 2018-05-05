package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;

import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.data.entity.Station;
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

    public static void loadImageInto(ImageView view, Station station) {
        Context context = view.getContext();
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), UiUtils.textAsBitmap(context, station.getName()));
        Glide.with(context)
                .load(station.getImageUrl())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(drawable)
                .into(view);
    }

    @WorkerThread
    public static Bitmap loadBitmapForStation(Context context, Station station) {
        Resources resources = context.getResources();
        int width = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
        int height = resources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);

        FutureTarget<Bitmap> futureTarget = Glide.with(context.getApplicationContext())
                .load(station.getImageUrl())
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(width, height);

        try {
            return futureTarget.get(3000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Timber.w("error loading image %s", station.getImageUrl());
            return textAsBitmap(context, station.getName());
        } finally {
            Glide.clear(futureTarget);
        }
    }

    public static int spToPx(Context context, int sp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.scaledDensity * sp);
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) (displayMetrics.density * dp);
    }

    public static Bitmap textAsBitmap(Context context, String text) {
        int maxTextLength = 4;
        int textSize = spToPx(context, 16);
        int padding = dpToPx(context, 4);

        CRC32 crc32 = new CRC32();
        crc32.update(text.getBytes());
        int randomDarkColor = getRandomDarkColor(crc32.getValue());

        text = text.substring(0, Math.min(text.length(), maxTextLength));

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(randomDarkColor);
        paint.setShadowLayer(2.0f, 2.0f, 2.0f, Color.WHITE);

        float baseLine = -paint.ascent() + padding;
        int width = (int) (paint.measureText(text) + 2 * padding);
        int height = (int) (baseLine + paint.descent() + 2 * padding);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, width / 2, baseLine, paint);
        return image;
    }

    public static int getRandomDarkColor(long seed) {
        double darkThreshold = 0.4;

        Random random = new Random(seed);
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        int color = Color.rgb(r, g, b);
        double luminance = ColorUtils.calculateLuminance(color);

        if (luminance > darkThreshold) return getRandomDarkColor(seed - 1);
        return color;
    }
}
