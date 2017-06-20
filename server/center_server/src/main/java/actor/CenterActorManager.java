package actor;


import timer.ActTimer;
import util.Randomizer;

import java.util.concurrent.ScheduledFuture;

public class CenterActorManager {
	private static CenterActorManager ourInstance = new CenterActorManager();

	public static CenterActorManager getInstance() {
		return ourInstance;
	}

	private ActTimer dbTimer = null;

	private ActTimer logicTimer = null;

	private IActor dbCheckActor = null;

	private ActorDispatcher dbActors = null;

	private ActorDispatcher dbLoadActors = null;

	private ActorDispatcher logicActors = null;

	private ActorDispatcher httpActors = null;

	private ActorDispatcher deskActors = null;

	private ActorDispatcher recordActors = null;

	private CenterActorManager() {
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
		dbActors = new ActorDispatcher(4, "db_actor_pool");
		if (!dbActors.start()) {
			return false;
		}
		logicActors = new ActorDispatcher(8, "logic_actor_pool");
		if (!logicActors.start()) {
			return false;
		}
		dbLoadActors = new ActorDispatcher(2, "db_load_pool");
		if (!dbLoadActors.start()) {
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

	public static IActor getDeskActor(int deskId) {
		return getInstance().deskActors.getActor(deskId);
	}

	public static IActor getHttpActor() {
		return getInstance().httpActors.getActor(0);
	}

	public static IActor getLogicActor(int playerId) {
		return getInstance().logicActors.getActor(playerId);
	}

	public static ScheduledFuture<?> registerOneTimeTask(long delay, Runnable run) {
		return getInstance().logicTimer.register(1000, delay, 1, run, getDbCheckActor(), "");
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

	public static IActor getRecordActor() {
		int randomNum = Randomizer.nextInt(2);
		return getInstance().dbActors.getActor(randomNum);
	}

	public static IActor getDbActor(int id) {
		return getInstance().dbActors.getActor(id);
	}

	public static IActor getLoadActor(int id) {
		return getInstance().dbLoadActors.getActor(id);
	}


}
