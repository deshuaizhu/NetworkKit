package com.che168.ahnetwork.http.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理类，待需求扩展
 *
 * @author zhudeshuai
 * @since 16/3/24
 */
public class ThreadPoolFactory {

    private static final String THREAD_NAME_IO = "thread_name_io";
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>()
    );

    /**
     * 处理后台IO操作
     *
     * @param runnable
     */
    public static void postBackgroundIORunnable(final ThreadRunnable runnable) {
        postBackgroundIORunnable(runnable, null);
    }

    /**
     * 处理后台IO操作
     *
     * @param runnable
     * @param callback 操作完成（主线程）
     */
    public static <T> void postBackgroundIORunnable(final ThreadRunnable<T> runnable, final ThreadCallback<T> callback) {
        THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName(THREAD_NAME_IO);
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                final T result = runnable.run();
                if (callback != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onThreadFinished(result);
                        }
                    });
                }
            }
        });
    }

    public interface ThreadRunnable<T> {
        T run();
    }

    public interface ThreadCallback<T> {
        void onThreadFinished(T result);
    }


}
