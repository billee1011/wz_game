package actor;


import timer.ActTimer;

public class GateActorManager implements IActorManager{
	private static GateActorManager ourInstance = new GateActorManager();

	public static GateActorManager getInstance() {
		return ourInstance;
	}

	private ActTimer gateTimer = null;

	private IActor pingActor = null;

	private GateActorManager() {
	}

	public boolean start() {
		gateTimer = new ActTimer("logic_timer");
		gateTimer.start();
		pingActor = new Actor("ping");
		if (!pingActor.start()) {
			return false;
		}
		return true;
	}

	@Override
	public String getStatus() {
		StringBuilder builder = new StringBuilder(32);
		builder.append(pingActor.getThreadName() + "=" + pingActor.getMaxQueueSize() + "\n");
		return builder.toString();
	}

	public static ActTimer getTimer() {
		return getInstance().gateTimer;
	}

	public static IActor getPingActor() {
		return getInstance().pingActor;
	}


}
