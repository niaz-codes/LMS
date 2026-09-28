package com.example.uos_lms.core.common;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Provides background threads for tasks that should not run on the main thread.
 * It is used for network calls and other background work.
 */
public final class AppExecutors {

    private static volatile AppExecutors instance;

    private final ExecutorService diskIO;
    private final Executor mainThread;

    private AppExecutors() {
        diskIO = Executors.newFixedThreadPool(4);
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainThread = mainHandler::post;
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    public ExecutorService diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }
}
