package io.github.vladimirmi.localradio.utils;

import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableMaybeObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;

/**
 * Created by Vladimir Mikhalev 11.04.2018.
 */

public class RxUtils {

    private RxUtils() {
    }

    public static class ErrorCompletableObserver extends DisposableCompletableObserver {

        private final Object errorHandler;

        public ErrorCompletableObserver(Object errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
            UiUtils.handleError(errorHandler, e);
        }
    }

    public static class ErrorSingleObserver<T> extends DisposableSingleObserver<T> {

        private final Object errorHandler;

        public ErrorSingleObserver(Object errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void onSuccess(T t) {
        }

        @Override
        public void onError(Throwable e) {
            UiUtils.handleError(errorHandler, e);
        }
    }

    public static class ErrorObserver<T> extends DisposableObserver<T> {

        private final Object errorHandler;

        public ErrorObserver(Object errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void onNext(T t) {
        }

        @Override
        public void onError(Throwable e) {
            UiUtils.handleError(errorHandler, e);
        }

        @Override
        public void onComplete() {
        }
    }

    public static class ErrorMaybeObserver<T> extends DisposableMaybeObserver<T> {

        private final Object errorHandler;

        public ErrorMaybeObserver(Object errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void onSuccess(T t) {
        }

        @Override
        public void onError(Throwable e) {
            UiUtils.handleError(errorHandler, e);
        }

        @Override
        public void onComplete() {
        }
    }
}
