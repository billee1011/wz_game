package timer;

import actor.IActor;
import actor.IRunner;
import actor.RunnableRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ActTimer {

    private final static Logger logger = LoggerFactory.getLogger(ActTimer.class);

    private final Object kMutex = new Object();

    private ScheduledThreadPoolExecutor executor = null;

    private String name = "";

    public ActTimer(String name) {
        this.name = name;
    }

    public void start() {
        start(2);
    }

    public boolean start(int threadCount) {
        synchronized (kMutex) {
            if (threadCount <= 0) {
                threadCount = 1;
            }
            if (executor != null && !executor.isShutdown() && !executor.isTerminated()
                && !executor.isTerminating()) {
                return false;
            }
            ScheduledThreadPoolExecutor exe = new ScheduledThreadPoolExecutor(threadCount,
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "ActTimer-" + name + "-" + threadNumber.getAndIncrement());
                    }
                });
            exe.setMaximumPoolSize(threadCount);
            exe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            // swap
            this.executor = exe;
            return true;
        }
    }

    public void stop() {
        synchronized (kMutex) {
            this.executor.shutdown();
        }
    }

    public boolean cancel(ScheduledFuture<?> future) {
        return cancel(future, true);
    }

    public boolean cancel(ScheduledFuture<?> future, boolean mayInterruptIfRunning) {
        if (future == null) {
            return false;
        }
        if (future instanceof RunnableScheduledFuture<?>) {
            synchronized (kMutex) {
                future.cancel(mayInterruptIfRunning);
                executor.remove((RunnableScheduledFuture<?>) future);
            }
        }
        return false;
    }

    public ScheduledFuture<?> register(long periodMS, long delay, Runnable r, IActor target, String name) {
        synchronized (kMutex) {
            return executor.scheduleAtFixedRate(new RepeatCountRunnable(r, target, name), delay, periodMS,
                TimeUnit.MILLISECONDS);
        }
    }

    public ScheduledFuture<?> register(long periodMS, Runnable r, IActor target, String name) {
        synchronized (kMutex) {
            return executor.scheduleAtFixedRate(new RepeatCountRunnable(r, target, name), 0, periodMS,
                TimeUnit.MILLISECONDS);
        }
    }

    public ScheduledFuture<?> register(long periodMS, long delay, int repeatLimit, Runnable r, IActor target,
        String name) {
        synchronized (kMutex) {
            RepeatCountRunnable runnable = new RepeatCountRunnable(r, target, repeatLimit, name);
            ScheduledFuture<?> future = executor.scheduleAtFixedRate(runnable, delay, periodMS,
                TimeUnit.MILLISECONDS);
            runnable.setScheduledFuture(future);
            return future;
        }
    }

    public ScheduledFuture<?> schedule(long delay, Runnable r, IActor target, String name) {
        synchronized (kMutex) {
            return executor.schedule(new RepeatCountRunnable(r, target, name), delay, TimeUnit.MILLISECONDS);
        }
    }

    public ScheduledFuture<?> scheduleAtTimestamp(long timestamp, Runnable r, IActor target, String name) {
        return schedule(timestamp - System.currentTimeMillis(), r, target, name);
    }

    private static class RepeatCountRunnable implements Runnable {
        private final IRunner r;
        private final IActor target;
        private final int repeatLimit; // 0 => infinite
        private int repeatCount = 0;
        private ScheduledFuture<?> scheduledFuture;
        private final String name;

        public RepeatCountRunnable(Runnable r, IActor target, String name) {
            this(r, target, 0, name);
        }

        public RepeatCountRunnable(Runnable r, IActor target, int repeatLimit, String name) {
            if (target == null) {
                throw new IllegalArgumentException("null actor in Timer Actor");
            }
            this.r = new RunnableRunner(r);
            this.repeatLimit = repeatLimit;
            this.target = target;
            this.name = name;
        }

        private boolean canRun() {
            if (repeatLimit == 0) {
                return true;
            }
            if (repeatCount >= repeatLimit) {
                return false;
            }
            ++repeatCount;
            return true;
        }

        @Override
        public void run() {
            if (canRun()) {
                try {
                    if (target != null) {
                        target.put(r);
                    }
                    else {
                        r.run();
                    }
                }
                catch (Throwable e) {
                    logger.error("", e);
                }
            }
            else if (scheduledFuture != null) {
                scheduledFuture.cancel(true);
            }
        }

        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        public String getName() {
            return name;
        }
    }

}
