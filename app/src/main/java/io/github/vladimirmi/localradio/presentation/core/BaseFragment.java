package io.github.vladimirmi.localradio.presentation.core;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public void onResume() {
        super.onResume();
        //noinspection unchecked
        presenter.attachView(this);
    }

    @Override
    public void onPause() {
        presenter.detachView();
        super.onPause();
    }

    @SuppressWarnings("all")
    @Override
    public Observable<Boolean> resolvePermissions(String... permissions) {
        return ((BaseActivity) getActivity()).resolvePermissions(permissions);
    }
}
