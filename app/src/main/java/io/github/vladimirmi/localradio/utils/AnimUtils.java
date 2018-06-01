package io.github.vladimirmi.localradio.utils;

import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.view.View;

/**
 * Created by Vladimir Mikhalev 01.06.2018.
 */
public class AnimUtils {

    private AnimUtils() {
    }

    public static SpringAnimation getBounceAnimation(View view, float startVelocity) {
        SpringAnimation spring = new SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 0)
                .setStartVelocity(startVelocity);
        spring.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_HIGH_BOUNCY);
        spring.getSpring().setStiffness(1000f);
        return spring;
    }
}
