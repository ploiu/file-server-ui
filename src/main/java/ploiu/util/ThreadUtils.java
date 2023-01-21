package ploiu.util;

public class ThreadUtils {
    private ThreadUtils() {
    }

    public static Thread runInThread(Runnable runnable) {
        var thread = new Thread(runnable);
        thread.start();
        return thread;
    }
}
