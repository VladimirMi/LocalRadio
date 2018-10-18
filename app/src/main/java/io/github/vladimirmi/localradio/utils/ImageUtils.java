package io.github.vladimirmi.localradio.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.domain.models.Station;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 01.06.2018.
 */
@SuppressWarnings("WeakerAccess")
public class ImageUtils {

    private ImageUtils() {
    }

    @SuppressLint("NewApi")
    public static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawableCompat || drawable instanceof VectorDrawable) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    public static void loadImageInto(ImageView view, Station station) {
        Context context = view.getContext();
        Resources resources = context.getResources();
        int width = resources.getDimensionPixelSize(R.dimen.icon_size);
        int height = resources.getDimensionPixelSize(R.dimen.icon_size);

        BitmapDrawable placeholder = new BitmapDrawable(resources,
                textAsBitmap(context, station.name));

        URL url;
        try {
            url = new URL(station.imageUrl);
        } catch (MalformedURLException e) {
            view.setImageDrawable(placeholder);
            return;
        }

        Glide.with(context)
                .load(url.toString())
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .placeholder(placeholder)
                .override(width, height)
                .into(view);
    }

    public static FutureTarget<Bitmap> getGlideTarget(Context context, Station station) {
        Resources resources = context.getResources();
        int width = resources.getDimensionPixelSize(R.dimen.icon_size);
        int height = resources.getDimensionPixelSize(R.dimen.icon_size);

        return Glide.with(context.getApplicationContext())
                .load(station.imageUrl)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .fitCenter()
                .into(width, height);
    }

    public static Observable<Bitmap> loadBitmapForStation(Context context, Station station) {
        return Observable.create((ObservableOnSubscribe<Bitmap>) emitter -> {
            FutureTarget<Bitmap> glideTarget = getGlideTarget(context, station);
            try {
                Bitmap bitmap = glideTarget.get(5000, TimeUnit.MILLISECONDS);
                if (!emitter.isDisposed()) {
                    emitter.onNext(bitmap);
                }
            } catch (Exception e) {
                Timber.w("error loading image %s", station.imageUrl);
            } finally {
                Glide.clear(glideTarget);
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Bitmap textAsBitmap(Context context, String text) {
        if (text.isEmpty()) {
            return getBitmap(context, R.drawable.ic_headphones);
        }

        int maxTextLength = 4;
        int textSize = UiUtils.spToPx(context, 16);
        int padding = UiUtils.dpToPx(context, 4);

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
