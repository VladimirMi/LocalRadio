package io.github.vladimirmi.localradio.presentation.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public abstract class BasePresenter<V extends BaseView> {

    protected V view;
    protected final CompositeDisposable disposables = new CompositeDisposable();
    private final CompositeDisposable disposablesOnFirstAttach = new CompositeDisposable();
    private boolean isFirstAttach = true;

    public final void attachView(V view) {
        this.view = view;
        if (isFirstAttach) {
            onFirstAttach(view, disposablesOnFirstAttach);
            isFirstAttach = false;
        }
        onAttach(view);
    }

    public final void detachView() {
        onDetach();
        disposables.clear();
        view = null;
    }

    public final void destroyView() {
        onDestroy();
        disposablesOnFirstAttach.clear();
        isFirstAttach = true;
    }

    protected void onFirstAttach(@Nullable V view, CompositeDisposable disposables) {
    }

    protected void onAttach(@NonNull V view) {
    }

    protected void onDetach() {
    }

    protected void onDestroy() {
    }
}
