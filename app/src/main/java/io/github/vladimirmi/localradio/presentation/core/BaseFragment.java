package io.github.vladimirmi.localradio.presentation.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.ResolvableApiException;

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
    public Observable<Boolean> resolvePermissions(String... permissions) {
        //noinspection unchecked,ConstantConditions
        return ((BaseActivity) getActivity()).resolvePermissions(permissions);
    }

    @Override
    public void showMessage(String message) {
        //noinspection ConstantConditions
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showMessage(int messageId) {
        //noinspection ConstantConditions
        Snackbar.make(getView(), messageId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void resolveApiException(ResolvableApiException resolvable) {
        //noinspection ConstantConditions
        ((BaseActivity) getActivity()).resolveApiException(resolvable);
    }
}
