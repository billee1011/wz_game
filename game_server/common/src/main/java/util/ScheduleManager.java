package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduleManager {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleManager.class);

    private String threadName;
    private ScheduledThreadPoolExecutor executor;

    public ScheduleManager(String name) {
        this.threadName = "Schedule-Worker";
        if (name != null) {
            this.threadName += "-" + name;
        }
    }

    public void start(int threadCount) {
        if (threadCount < 1) {
            throw new IllegalArgumentException("invalid threadCount");
        }
        if (executor != null && !executor.isShutdown() && !executor.isTerminated()) {
            return;
        }
        executor = new ScheduledThreadPoolExecutor(threadCount, new ThreadFactory() {

            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName(threadName + "-" + threadNumber.getAndIncrement());
                return t;
            }
        });
        executor.setMaximumPoolSize(threadCount);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    }

    public void stop() {
        executor.shutdown();
    }

    public ScheduledFuture<?> register(long repeatTimeMilliSeconds, long delayMilliSeconds, Runnable r,
        String name) {
        return executor.scheduleAtFixedRate(new SilentRunnable(r, name), delayMilliSeconds,
            repeatTimeMilliSeconds, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> register(long repeatTimeMilliSeconds, Runnable r, String name) {
        return executor.scheduleAtFixedRate(new SilentRunnable(r, name), 0, repeatTimeMilliSeconds,
            TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(long delayMilliSeconds, Runnable r, String name) {
        return executor.schedule(new SilentRunnable(r, name), delayMilliSeconds, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> scheduleAtTimestamp(long timestamp, Runnable r, String name) {
        return schedule(timestamp - System.currentTimeMillis(), r, name);
    }

    public long getActiveCount() {
        return executor.getActiveCount();
    }

    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    public int getQueuedTasks() {
        return executor.getQueue().toArray().length;
    }

    public long getTaskCount() {
        return executor.getTaskCount();
    }

    public boolean isShutdown() {
        return executor.isShutdown();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    private static class SilentRunnable implements Runnable {

        private final Runnable r;
        // private final String name;

        public SilentRunnable(Runnable r, String name) {
            this.r = r;
            // this.name = name;
        }

        @Override
        public void run() {
            try {
                r.run();
            }
            catch (Throwable t) {
                // ignore
                logger.error("", t);
            }
        }
    }
}
