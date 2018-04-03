package io.github.vladimirmi.localradio.presentation.core;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Vladimir Mikhalev 02.03.2018.
 */

public abstract class BasePresenter<V extends BaseView> {

    protected V mView;
    protected final CompositeDisposable mCompDisp = new CompositeDisposable();

    public final void attachView(V view) {
        this.mView = view;
        onAttach(view);
    }

    public final void detachView() {
        onDetach();
        mCompDisp.clear();
        mView = null;
    }

    protected void onAttach(V view) {
    }

    protected void onDetach() {
        //no-op
    }
}
