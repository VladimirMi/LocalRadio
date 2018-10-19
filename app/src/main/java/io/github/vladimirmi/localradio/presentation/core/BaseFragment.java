package io.github.vladimirmi.localradio.presentation.core;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.material.snackbar.Snackbar;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;


/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public abstract class BaseFragment<P extends BasePresenter> extends Fragment implements BaseView {

    protected P presenter;
    private Unbinder unbinder;

    protected abstract int getLayout();

    protected abstract P providePresenter();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = providePresenter();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupView(view);
    }

    protected abstract void setupView(View view);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();
        //noinspection unchecked
        presenter.attachView(this);
    }

    @Override
    public void onStop() {
        presenter.detachView();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (getActivity() != null && getActivity().isFinishing()) {
            presenter.destroy();
            presenter = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean handleBackPress() {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            if (fragment instanceof BaseView && ((BaseView) fragment).handleBackPress()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Observable<Permission> resolvePermissions(String... permissions) {
        return new RxPermissions(this).requestEachCombined(permissions);
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int messageId) {
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void resolveApiException(ResolvableApiException resolvable) {
        ((BaseActivity) getActivity()).resolveApiException(resolvable);
    }
}
