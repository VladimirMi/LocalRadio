package io.github.vladimirmi.localradio.utils;

import android.graphics.Outline;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewOutlineProvider;

import io.github.vladimirmi.localradio.R;

/**
 * Created by Vladimir Mikhalev 30.05.2018.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FixedOutlineProvider extends ViewOutlineProvider {

    @Override
    public void getOutline(View view, Outline outline) {
        outline.setRect(
                0,
                view.getResources().getDimensionPixelSize(R.dimen.item_card_elevation),
                view.getWidth(),
                view.getHeight()
        );
    }
}
