package actor;

public class ActorDispatcher {

    private final Actor[] actors;

    public ActorDispatcher(int poolSize, String name) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException();
        }
        actors = new Actor[poolSize];
        for (int i = 0; i < actors.length; ++i) {
            actors[i] = new Actor("Dispatcher-" + name + "-" + i);
        }
    }

    public ActorDispatcher(int poolSize, String name, int capacity) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException();
        }
        actors = new Actor[poolSize];
        for (int i = 0; i < actors.length; ++i) {
            actors[i] = new Actor("Dispatcher-" + name + "-" + i, capacity);
        }
    }

    public IActor getActor(int dispatchId) {
        int idx = Math.abs(dispatchId) % actors.length;
        return actors[idx];
    }

    public void put(int dispatchId, IRunner runner) {
        getActor(dispatchId).put(runner);
    }

    public void put(int dispatchId, IRunner runner, ICallback callback, IActor target) {
        getActor(dispatchId).put(runner, callback, target);
    }

    public boolean start() {
        for (int i = 0; i < actors.length; ++i) {
            if (false == actors[i].start()) {
                return false;
            }
        }
        return true;
    }

    public void stop() {
        for (int i = 0; i < actors.length; ++i) {
            actors[i].stop();
        }
    }

    public void stopWhenEmpty() {
        for (int i = 0; i < actors.length; ++i) {
            actors[i].stopWhenEmpty();
        }
    }

    public void waitForStop() {
        while (isRunning()) {
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {
                break;
            }
        }
    }

    public boolean isStopping() {
        for (int i = 0; i < actors.length; ++i) {
            if (actors[i].isStopping()) {
                return true;
            }
        }
        return false;
    }

    public boolean isRunning() {
        for (int i = 0; i < actors.length; ++i) {
            if (actors[i].isRunning()) {
                return true;
            }
        }
        return false;
    }

    public String getActorStatus() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < actors.length; ++i) {
            ret.append(actors[i].getThreadName() + "=" + actors[i].getMaxQueueSize() + "|");
        }
        return ret.toString();
    }
}
