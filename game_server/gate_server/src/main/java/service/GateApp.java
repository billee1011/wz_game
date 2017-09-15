package service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.GateActorManager;
import actor.IActor;
import actor.IActorManager;
import actor.ICallback;
import define.AppId;
import network.AbstractHandlers;
import service.handler.GateClientHandler;
import service.handler.GateServerHandler;
import service.handler.agent.AgentManager;
import service.handler.codec.GateDecoder;
import service.handler.network.GateNetServer;
import timer.ActTimer;

/**
 * Created by Administrator on 2017/2/6.
 */
public class GateApp extends BaseApp {
	private static Logger logger = LoggerFactory.getLogger(GateApp.class);
	private GateNetServer netServer;

	private static GateApp instance = new GateApp();

	private GateApp() {
	}

	public static GateApp getInst() {
		return instance;
	}
	
	@Override
	protected List<String> getRegistParam() {
		String host = props.getString("domain", "");
		int port = props.getInteger("gate.port", 9091);
		logger.info(" init gate app and  app bind the port {}", port);
		List<String> params = new ArrayList<String>();
		params.add(host);
		params.add(port+"");
		return params;
	}

	@Override
	protected void beforeStart() {

	}

	@Override
	protected void initServer() {
		int port = props.getInteger("gate.port", 9091);
		
		GateActorManager.getInstance().start();
		netServer = new GateNetServer(port);
		netServer.setDecoder(new GateDecoder());
		netServer.setHandler(new GateServerHandler());
		netServer.start();
	}

	@Override
	protected String getServerName() {
		return "gate_server";
	}

	@Override
	protected void afterStart() {

	}

	@Override
	protected AbstractHandlers getClientHandler() {
		return new GateClientHandler();
	}

	@Override
	protected AppId getAppId() {
		return AppId.GATE;
	}

	public static void main(String[] args) {
		GateApp.getInst().start();
	}


	@Override
	protected IActor getCrontabActor() {
		return GateActorManager.getPingActor();
	}

	@Override
	protected ActTimer getCrontabTimer() {
		return GateActorManager.getTimer();
	}

	@Override
	protected IActorManager getActorManager() {
		return GateActorManager.getInstance();
	}

	@Override
	public void stop() {
		AgentManager.getInst().closeAll();
		System.exit(0);
	}

	@Override
	protected ICallback getSessionCloseCallBack() {
		return (e)->{
			AgentManager.getInst().closeAll();
		};
	}
	
}
