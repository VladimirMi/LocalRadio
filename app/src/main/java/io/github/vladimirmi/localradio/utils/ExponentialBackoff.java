package io.github.vladimirmi.localradio.utils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Vladimir Mikhalev 01.11.2018.
 */
public class ExponentialBackoff {

    private static final int TRIES = 4;
    private int counter;
    private TimerTask task;

    public boolean schedule(Runnable runnable) {
        if (task != null) task.cancel();

        if (counter == TRIES) {
            counter = 0;
            task = null;
            return false;
        }

        task = new TimerTask() {
            @Override
            public void run() {
                counter++;
                runnable.run();
            }
        };
        new Timer().schedule(task, calcBackoff(counter));
        return true;
    }

    private long calcBackoff(int n) {
        return (long) (Math.pow(2.0, n) * 1000);
    }

}
