package acotr;

import actor.Actor;
import actor.ActorDispatcher;
import actor.IActor;
import actor.IActorManager;
import timer.ActTimer;

/**
 * Created by think on 2017/4/11.
 */
public class LogActorManager implements IActorManager {

	private static LogActorManager inst = new LogActorManager();

	private LogActorManager() {

	}

	public static LogActorManager getInst() {
		return inst;
	}

	private ActorDispatcher saveActors;

	private ActTimer timer;

	private IActor actor;


	public boolean start() {
		saveActors = new ActorDispatcher(16, "log_save");
		if (!saveActors.start()) {
			return false;
		}
		actor = new Actor("ping");
		if (!actor.start()) {
			return false;
		}
		timer = new ActTimer("update");
		timer.start();
		return true;
	}

	@Override
	public String getStatus() {
		StringBuilder builder = new StringBuilder(256);
		builder.append(actor.getThreadName() + "=" + actor.getMaxQueueSize() + "\n");
		builder.append(saveActors.getActorStatus() + "\n");
		return builder.toString();
	}

	public static ActTimer getTimer() {
		return getInst().timer;
	}

	public static IActor getActor() {
		return getInst().actor;
	}

	public static IActor getSaveActor(int actorId) {
		return getInst().saveActors.getActor(actorId);
	}
}
