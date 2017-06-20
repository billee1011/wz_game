package actor;

public class ActorTask {
    public final IRunner runner;
    public final ICallback callback;
    public final IActor target;

    /**
     * 在target上执行callback
     * 
     * @param runner
     * @param callback
     * @param target
     */
    public ActorTask(final IRunner runner, final ICallback callback, final IActor target) {
        this.runner = runner;
        this.callback = callback;
        this.target = target;
    }

    /**
     * 不需要callback
     * 
     * @param runner
     */
    public ActorTask(final IRunner runner) {
        this.runner = runner;
        this.callback = null;
        this.target = null;
    }

    /**
     * 不需要callback
     * 
     * @param r
     */
    public ActorTask(final Runnable r) {
        this(new RunnableRunner(r));
    }
}
