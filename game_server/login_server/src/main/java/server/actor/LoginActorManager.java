package server.actor;


import actor.Actor;
import actor.ActorDispatcher;
import actor.IActor;
import actor.IActorManager;
import timer.ActTimer;

import java.util.concurrent.ScheduledFuture;

public class LoginActorManager implements IActorManager {
	private static LoginActorManager ourInstance = new LoginActorManager();

	public static LoginActorManager getInstance() {
		return ourInstance;
	}

	private ActTimer loginTimer = null;

	private IActor pingActor = null;

	private LoginActorManager() {
	}

	public boolean start() {
		loginTimer = new ActTimer("logic_timer");
		loginTimer.start();
		pingActor = new Actor("ping");
		if (!pingActor.start()) {
			return false;
		}
		return true;
	}

	@Override
	public String getStatus() {
		StringBuilder builder = new StringBuilder(256);
		builder.append(pingActor.getThreadName() + "=" + pingActor.getMaxQueueSize() + "\n");
		return builder.toString();
	}

	public static ActTimer getTimer() {
		return getInstance().loginTimer;
	}

	public static IActor getPingActor() {
		return getInstance().pingActor;
	}


}
