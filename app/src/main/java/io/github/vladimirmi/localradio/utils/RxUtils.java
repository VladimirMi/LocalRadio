package io.github.vladimirmi.localradio.utils;

import io.github.vladimirmi.localradio.presentation.core.BaseView;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 11.04.2018.
 */

public class RxUtils {

    private RxUtils() {
    }

    public static class ErrorCompletableObserver extends DisposableCompletableObserver {

        private final BaseView view;

        public ErrorCompletableObserver(BaseView view) {
            this.view = view;
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e);
            if (view != null && e instanceof MessageException) {
                view.showMessage(((MessageException) e).getMessageId());
            }
        }
    }

    public static class ErrorSingleObserver<T> extends DisposableSingleObserver<T> {

        private final BaseView view;

        public ErrorSingleObserver(BaseView view) {
            this.view = view;
        }

        @Override
        public void onSuccess(T t) {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e);
            if (view != null && e instanceof MessageException) {
                view.showMessage(((MessageException) e).getMessageId());
            }
        }
    }

    public static class ErrorObservableObserver<T> extends DisposableObserver<T> {

        private final BaseView view;

        public ErrorObservableObserver(BaseView view) {
            this.view = view;
        }

        @Override
        public void onNext(T t) {

        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e);
            if (view != null && e instanceof MessageException) {
                view.showMessage(((MessageException) e).getMessageId());
            }
        }

        @Override
        public void onComplete() {

        }
    }
}
