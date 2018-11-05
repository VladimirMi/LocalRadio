package io.github.vladimirmi.localradio.utils;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import android.view.View;

/**
 * Created by Vladimir Mikhalev 01.06.2018.
 */
public class AnimUtils {

    private AnimUtils() {
    }

    public static SpringAnimation getBounceAnimation(View view, int dpVelocity) {
        SpringAnimation spring = new SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 0)
                .setStartVelocity(UiUtils.dpToPx(view.getContext(), dpVelocity));
        spring.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        spring.getSpring().setStiffness(1000f);
        return spring;
    }

    public static SpringAnimation getScaleXAnimation(View view, int dpVelocity) {
        SpringAnimation spring = new SpringAnimation(view, DynamicAnimation.SCALE_X, 1)
                .setStartVelocity(UiUtils.dpToPx(view.getContext(), dpVelocity));
        spring.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        spring.getSpring().setStiffness(600f);
        return spring;
    }

    public static SpringAnimation getScaleYAnimation(View view, int dpVelocity) {
        SpringAnimation spring = new SpringAnimation(view, DynamicAnimation.SCALE_Y, 1)
                .setStartVelocity(UiUtils.dpToPx(view.getContext(), dpVelocity));
        spring.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        spring.getSpring().setStiffness(600f);
        return spring;
    }
}
