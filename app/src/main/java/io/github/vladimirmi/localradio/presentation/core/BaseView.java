package io.github.vladimirmi.localradio.presentation.core;

import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public interface BaseView {

    Observable<Boolean> resolvePermissions(String... permissions);
}
