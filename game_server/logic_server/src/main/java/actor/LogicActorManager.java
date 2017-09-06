package actor;


import timer.ActTimer;

import java.util.concurrent.ScheduledFuture;

public class LogicActorManager implements IActorManager {
	private static LogicActorManager ourInstance = new LogicActorManager();

	public static LogicActorManager getInstance() {
		return ourInstance;
	}

	private ActTimer logicTimer = null;


	private ActorDispatcher logicActors = null;

	private ActorDispatcher httpActors = null;

	private ActorDispatcher deskActors = null;

	private ActorDispatcher recordActors = null;

	private LogicActorManager() {
	}

	public boolean start() {
		logicTimer = new ActTimer("logic_timer");
		logicTimer.start();
		logicActors = new ActorDispatcher(2, "logic_actor_pool");
		if (!logicActors.start()) {
			return false;
		}
		httpActors = new ActorDispatcher(2, "http_pool");
		if (!httpActors.start()) {
			return false;
		}
		deskActors = new ActorDispatcher(8, "desk_logic");
		if (!deskActors.start()) {
			return false;
		}
		recordActors = new ActorDispatcher(2, "record");
		if (!recordActors.start()) {
			return false;
		}
		return true;
	}

	@Override
	public String getStatus() {
		StringBuilder builder = new StringBuilder(256);
		builder.append(recordActors.getActorStatus() + "\n");
		builder.append(logicActors.getActorStatus() + "\n");
		builder.append(httpActors.getActorStatus() + "\n");
		builder.append(deskActors.getActorStatus() + "\n");
		return builder.toString();
	}

	public static ActTimer getTimer() {
		return getInstance().logicTimer;
	}

	public static IActor getLogicActor() {
		return getInstance().logicActors.getActor(1);
	}

	public static IActor getLogicActor_ex() {
		return getInstance().logicActors.getActor(1);
	}

	public static IActor getDeskActor(int deskId) {
		return getInstance().deskActors.getActor(deskId);
	}

//	public static ScheduledFuture<?> registerOneTimeTask(long delay, Runnable run) {
//		return registerOneTimeTask(delay, run, getDeskActor(0));
//	}
//
//	public static ScheduledFuture<?> registerOneTimeTask(long delay, Runnable run, IActor target) {
//		return getInstance().logicTimer.register(1000, delay, 1, run, getDeskActor(0), "");
//	}

	public static ScheduledFuture<?> registerOneTimeTask(long delay, Runnable run, int deskId) {
		return getInstance().logicTimer.register(1000, delay, 1, run, getDeskActor(deskId), "");
	}


}
