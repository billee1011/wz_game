package actor;

public interface IActor {

    boolean start();

    void clear();

    void stop();

    void stopWhenEmpty();

    void waitForStop();

    boolean put(IRunner runner);

    /**
     * @param millisec
     *            0 - 不等待, 负数 - 无限等待, 正数 - 最长等待毫秒数
     */
    boolean put(IRunner runner, long millisec);

    boolean put(IRunner runner, ICallback callback, IActor target);

    boolean put(IRunner runner, ICallback callback, IActor target, long millisec);

    long getThreadId();

    String getThreadName();

    int getQueueSize();

    int getMaxQueueSize();

    boolean isRunning();

    boolean isStopping();
}
