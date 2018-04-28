package io.github.vladimirmi.localradio.utils;

import android.content.Context;
import android.widget.Toast;

import java.net.SocketTimeoutException;

import io.github.vladimirmi.localradio.R;
import io.github.vladimirmi.localradio.presentation.core.BaseView;
import timber.log.Timber;

/**
 * Created by Vladimir Mikhalev 28.04.2018.
 */

public class UiUtils {

    private UiUtils() {
    }

    public static void handleError(Object errorHandler, Throwable e) {
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
