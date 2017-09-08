package actor;


import java.util.concurrent.ScheduledFuture;

import timer.ActTimer;

public class CenterActorManager implements IActorManager {
    private static CenterActorManager ourInstance = new CenterActorManager();

    public static CenterActorManager getInstance() {
        return ourInstance;
    }

    private ActTimer dbTimer = null;

    private ActTimer logicTimer = null;

    private IActor dbCheckActor = null;

    private IActor updateActor = null;

    private ActorDispatcher dbActors = null;

    private ActorDispatcher dbLoadActors = null;

    private ActorDispatcher logicActors = null;

    private ActorDispatcher httpActors = null;

    private ActorDispatcher deskActors = null;

    private static int httpActorNum = 0;


    private CenterActorManager() {
    }

    public boolean stopWhenEmpty() {
        dbTimer.stop();
        logicTimer.stop();
        dbCheckActor.stopWhenEmpty();
        dbActors.stopWhenEmpty();
        dbLoadActors.stopWhenEmpty();
        logicActors.stopWhenEmpty();
        httpActors.stopWhenEmpty();
        deskActors.stopWhenEmpty();
        updateActor.stopWhenEmpty();
        return true;
    }

    public boolean waitForStop() {
        dbCheckActor.waitForStop();
        dbActors.waitForStop();
        dbLoadActors.waitForStop();
        logicActors.waitForStop();
        httpActors.waitForStop();
        deskActors.waitForStop();
        updateActor.waitForStop();
        return true;
    }


    public boolean start() {
        dbTimer = new ActTimer("db_timer");
        logicTimer = new ActTimer("logic_timer");
        dbTimer.start();
        logicTimer.start();
        dbCheckActor = new Actor("db_check_actor");
        if (!dbCheckActor.start()) {
            return false;
        }
        dbActors = new ActorDispatcher(8, "db_actor_pool");
        if (!dbActors.start()) {
            return false;
        }
        logicActors = new ActorDispatcher(8, "logic_actor_pool");
        if (!logicActors.start()) {
            return false;
        }
        dbLoadActors = new ActorDispatcher(4, "db_load_pool");
        if (!dbLoadActors.start()) {
            return false;
        }
        httpActors = new ActorDispatcher(24, "http_pool");
        if (!httpActors.start()) {
            return false;
        }
        deskActors = new ActorDispatcher(8, "desk_logic");
        if (!deskActors.start()) {
            return false;
        }
        updateActor = new Actor("update_agent");
        if (!updateActor.start()) {
            return false;
        }
        return true;
    }

    @Override
    public String getStatus() {
        StringBuilder builder = new StringBuilder(256);
        builder.append(dbCheckActor.getThreadName() + "=" + dbCheckActor.getMaxQueueSize() + "\n");
        builder.append(updateActor.getThreadName() + "=" + updateActor.getMaxQueueSize() + "\n");
        builder.append(dbActors.getActorStatus() + "\n");
        builder.append(dbLoadActors.getActorStatus() + "\n");
        builder.append(logicActors.getActorStatus() + "\n");
        builder.append(httpActors.getActorStatus() + "\n");
        builder.append(deskActors.getActorStatus() + "\n");
        return builder.toString();
    }

    public static ActorDispatcher getDBAcotrs() {
        return getInstance().dbActors;
    }

    public static IActor getDeskActor(int deskId) {
        return getInstance().deskActors.getActor(deskId);
    }

    public static IActor getHttpActor() {
        httpActorNum++;
        return getInstance().httpActors.getActor(httpActorNum);
    }

    public static IActor getLogicActor(long playerId) {
        return getInstance().logicActors.getActor((int) playerId);
    }

    public static ScheduledFuture<?> registerOneTimeTaskDeskThread(long delay, Runnable run) {
        return getInstance().logicTimer.register(1000, delay, 1, run, getDeskActor(), "");
    }

    public static ScheduledFuture<?> registerOneTimeTask(long delay, Runnable run, int deskId) {
        return getInstance().logicTimer.register(1000, delay, 1, run, getDeskActor(deskId), "");
    }

    public static ActTimer getLogicTimer() {
        return getInstance().logicTimer;
    }

    public static ActTimer getDBTimer() {
        return getInstance().dbTimer;
    }

    public static IActor getDbCheckActor() {
        return getInstance().dbCheckActor;
    }

    public static IActor getUpdateActor() {
        return getInstance().updateActor;
    }

    public static IActor getDbActor(int id) {
        return getInstance().dbActors.getActor(id);
    }

    public static IActor getLoadActor(int id) {
        return getInstance().dbLoadActors.getActor(id);
    }

    public static IActor getDeskActor() {
        return getInstance().deskActors.getActor(0);
    }

}
