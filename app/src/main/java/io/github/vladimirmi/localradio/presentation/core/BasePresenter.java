package io.github.vladimirmi.localradio.presentation.core;

import android.support.annotation.Nullable;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public abstract class BasePresenter<V extends BaseView> {

    protected V view;
    protected final CompositeDisposable disposables = new CompositeDisposable();
    private boolean isFirstAttach = true;

    public final void attachView(V view) {
        this.view = view;
        onAttach(view, isFirstAttach);
        if (isFirstAttach) {
            onFirstAttach(view);
            isFirstAttach = false;
        }
    }

    public final void detachView() {
        onDetach();
        disposables.clear();
        view = null;
    }

    public final void destroyView() {
        onDestroy();
        isFirstAttach = true;
    }

    protected void onFirstAttach(V view) {
    }

    protected void onAttach(V view, boolean isFirstAttach) {
    }

    protected void onDetach() {
    }

    protected void onDestroy() {
    }

    @Nullable
    protected V getView() {
        return view;
    }

    protected boolean hasView() {
        return getView() != null;
    }
}
