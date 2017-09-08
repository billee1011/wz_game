package actor;


import timer.ActTimer;

import java.util.concurrent.ScheduledFuture;

public class LogicActorManager implements IActorManager {
	private static LogicActorManager ourInstance = new LogicActorManager();

	public static LogicActorManager getInstance() {
		return ourInstance;
	}

	private ActTimer logicTimer = null;

	private ActTimer dbTimer = null;

	private IActor dbCheckActor = null;

	private ActorDispatcher loadActors = null;

	private ActorDispatcher dbActors = null;

	private LogicActorManager() {
	}

	public boolean start() {
		logicTimer = new ActTimer("logic_timer");
		logicTimer.start();
		dbTimer = new ActTimer("db_check");
		dbTimer.start();
		dbCheckActor = new Actor("db_check");
		if (!dbCheckActor.start()) {
			return false;
		}
		loadActors = new ActorDispatcher(2, "load_db");
		if (!loadActors.start()) {
			return false;
		}
		dbActors = new ActorDispatcher(4, "db_actor");
		if (!dbActors.start()) {
			return false;
		}
		return true;
	}

	@Override
	public String getStatus() {
		StringBuilder builder = new StringBuilder(256);
		builder.append(dbActors.getActorStatus() + "\n");
		builder.append(loadActors.getActorStatus() + "\n");
		return builder.toString();
	}

	public static IActor getDBCheckActor() {
		return getInstance().dbCheckActor;
	}


	public static IActor getDBLoadActor(long id) {
		return getInstance().loadActors.getActor((int) id);
	}

	public static IActor getDBActor(long id) {
		return getInstance().dbActors.getActor((int) id);
	}

	public static ActTimer getTimer() {
		return getInstance().logicTimer;
	}

	public static IActor getLogicActor() {
		return getInstance().loadActors.getActor(1);
	}


	public static ActTimer getDBTimer() {
		return getInstance().dbTimer;
	}

}
