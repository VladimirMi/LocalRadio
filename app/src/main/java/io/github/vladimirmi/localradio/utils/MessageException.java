package io.github.vladimirmi.localradio.utils;

import android.content.Context;

/**
 * Created by Vladimir Mikhalev 11.04.2018.
 */
public class MessageException extends RuntimeException {

    private final int messageId;

    public MessageException(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getMessage(Context context) {
        return context.getString(messageId);
    }
}
