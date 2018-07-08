package io.github.vladimirmi.localradio.presentation.core;

import android.support.annotation.Nullable;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

@SuppressWarnings("WeakerAccess")
public abstract class BasePresenter<V extends BaseView> {

    protected V view;
    protected final CompositeDisposable viewSubs = new CompositeDisposable();
    protected final CompositeDisposable dataSubs = new CompositeDisposable();
    private boolean isFirstAttach = true;

    public final void attachView(V view) {
        this.view = view;
        if (isFirstAttach) {
            onFirstAttach(view, dataSubs);
            isFirstAttach = false;
        }
        onAttach(view);
    }

    public final void detachView() {
        onDetach();
        viewSubs.clear();
        view = null;
    }

    public final void destroy() {
        onDestroy();
        isFirstAttach = true;
        dataSubs.clear();
    }

    protected void onFirstAttach(V view, CompositeDisposable disposables) {
    }

    protected void onAttach(V view) {
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
