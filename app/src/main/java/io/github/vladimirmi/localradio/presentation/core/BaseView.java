package io.github.vladimirmi.localradio.presentation.core;

import com.google.android.gms.common.api.ResolvableApiException;
import com.tbruyelle.rxpermissions2.Permission;

import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public interface BaseView {

    Observable<Permission> resolvePermissions(String... permissions);

    void showMessage(String message);

    void showMessage(int messageId);

    void resolveApiException(ResolvableApiException resolvable);

    boolean handleBackPress();
}
