package io.github.vladimirmi.localradio.presentation.core;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import androidx.fragment.app.Fragment;
import io.reactivex.Observable;

/**
 * Created by Vladimir Mikhalev 31.10.2018.
 */
public class FragmentBaseViewDelegate implements BaseView {

    private Fragment fragment;

    public FragmentBaseViewDelegate(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public boolean handleBackPress() {
        for (Fragment fragment : fragment.getChildFragmentManager().getFragments()) {
            if (fragment instanceof BaseView && ((BaseView) fragment).handleBackPress()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Observable<Permission> resolvePermissions(String... permissions) {
        return new RxPermissions(fragment).requestEachCombined(permissions);
    }

    @Override
    public void showMessage(String message) {
        fragment.getActivity().runOnUiThread(() -> {
            Snackbar.make(fragment.getView(), message, Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    public void showMessage(int messageId) {
        fragment.getActivity().runOnUiThread(() -> {
            Snackbar.make(fragment.getView(), messageId, Snackbar.LENGTH_SHORT).show();
        });
    }

    @Override
    public void resolveApiException(ResolvableApiException resolvable) {
        ((BaseActivity) fragment.getActivity()).resolveApiException(resolvable);
    }
}
