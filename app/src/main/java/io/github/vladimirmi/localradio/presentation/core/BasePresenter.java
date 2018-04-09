package io.github.vladimirmi.localradio.presentation.core;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public abstract class BasePresenter<V extends BaseView> {

    protected V view;
    protected final CompositeDisposable compDisp = new CompositeDisposable();

    public final void attachView(V view) {
        this.view = view;
        onAttach(view);
    }

    public final void detachView() {
        onDetach();
        compDisp.clear();
        view = null;
    }

    protected void onAttach(V view) {
    }

    protected void onDetach() {
    }
}
