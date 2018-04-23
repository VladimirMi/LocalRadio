package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.widget.Toast;

import java.net.SocketTimeoutException;

import io.github.vladimirmi.localradio.R;
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

        private final Object errorHandler;

        public ErrorCompletableObserver(Object errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void onComplete() {
        }

        @Override
        public void onError(Throwable e) {
            handleError(errorHandler, e);
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
            handleError(errorHandler, e);
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
            handleError(errorHandler, e);
        }

        @Override
        public void onComplete() {
        }
    }

    private static void handleError(Object errorHandler, Throwable e) {
        int messageId = -1;
        if (e instanceof MessageException) {
            messageId = ((MessageException) e).getMessageId();
        } else if (e instanceof SocketTimeoutException) {
            messageId = R.string.error_network;
        }

        if (errorHandler != null && messageId != -1) {
            if (errorHandler instanceof BaseView) {
                ((BaseView) errorHandler).showMessage(messageId);
            } else if (errorHandler instanceof Context) {
                Toast.makeText((Context) errorHandler, messageId, Toast.LENGTH_SHORT).show();
            }
        } else {
            Timber.e(e);
        }
    }
}
