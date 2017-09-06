package network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import define.AppId;

/**
 * Created by Administrator on 2017/2/4.
 */
public class ServerManager {
	private static final Logger logger = LoggerFactory.getLogger(ServerManager.class);
	
	private static ServerManager instance = new ServerManager();

	private ServerManager() {

	}

	private Map<AppId, AtomicInteger> idGenMap = new HashMap<>();


	public static ServerManager getInst() {
		return instance;
	}

	private Map<AppId, Map<Integer, ServerSession>> sessionMap = new HashMap<>();

//	public int registerServer(ServerSession session) {
//		Map<Integer, ServerSession> appSessionMap = this.sessionMap.get(session.getAppId());
//		if (appSessionMap == null) {
//			appSessionMap = new HashMap<>();
//			this.sessionMap.put(session.getAppId(), appSessionMap);
//		}
//		int dynamicId = geneAppDynamicId(session.getAppId());
////		if (session.getAppId() == AppId.LOGIC && dynamicId == 1) {
////			session.setLoadFactor(1000000);
////		}
//		session.setServerId(dynamicId);
//		appSessionMap.put(dynamicId, session);
//		return dynamicId;
//	}

	private int geneAppDynamicId(AppId id) {
		AtomicInteger idGen = idGenMap.get(id);
		if (idGen == null) {
			idGen = new AtomicInteger(1);
			idGenMap.put(id, idGen);
		}
		return idGen.getAndIncrement();
	}

	public void registerServer(long centerStartTime,ServerSession session, List<String> registParam) {
		Map<Integer, ServerSession> appSessionMap = sessionMap.get(session.getAppId());
		if (appSessionMap == null) {
			appSessionMap = new HashMap<>();
			sessionMap.put(session.getAppId(), appSessionMap);
		}
		int oldDynamicId = Integer.parseInt(registParam.get(1));
		long oldCenterStartTime = Long.valueOf(registParam.get(2));
		int dynamicId = 0;

		if(session.getAppId() == AppId.GATE){
			String host = registParam.get(3);
			int port = Integer.parseInt(registParam.get(4));
			session.setLocalPort(port);
			session.setRemoteAddress(host);
		}
		
		if(oldCenterStartTime == centerStartTime){//client断开 center未重启
			dynamicId = oldDynamicId;
			logger.info("{}服务器,断线之后重连,当前连接信息{}",session.getAppId(),registParam);
		}else{
			//第一次连接或者center重启过
			if(oldCenterStartTime == 0){
				logger.info("{}服务器,第一次连接,当前连接信息{}",session.getAppId(),registParam);
			}else{
				logger.info("{}服务器,断线之后重连,之前center有重启过,当前连接信息{}",session.getAppId(),registParam);
			}
			dynamicId = geneAppDynamicId(session.getAppId());
		}
		
		session.setServerId(dynamicId);
		appSessionMap.put(dynamicId, session);
	}

//	private boolean containServerSession(List<ServerSession> list, ServerSession session) {
//		for (ServerSession s : list) {
//			if (s.getRemoteAddress().equals(session.getRemoteAddress()) && s.getRemotePort() == session.getRemotePort()) {
//				return true;
//			}
//		}
//		return false;
//	}

	public List<ServerSession> getSessionList(AppId id) {
		Map<Integer, ServerSession> sessions = sessionMap.get(id);
		if (sessions == null) {
			return null;
		}
		return new ArrayList<>(sessions.values());
	}


	public void removeServerSession(ServerSession session) {
		AppId id = session.getAppId();
		Map<Integer, ServerSession> sessionList = sessionMap.get(id);
		if (sessionList != null) {
			//根据key 监听到超时连接断开之后，之后gate又重连，导致覆盖key，但是超时这个会删除key
			sessionList.values().remove(session);
		}
	}

//	public void addServerSessionLoadFactor(ChannelHandlerContext ctx) {
//		ServerSession session = NettyUtil.getAttribute(ctx, ServerSession.KEY);
//		if (session != null) {
//			session.addLoadFactor();
//		}
//	}

//	public ServerSession getNiuniuSession(int originalId) {
//		if (originalId == 0) {
//			return getMinLoadSession(AppId.LOGIC);
//		}
//		Map<Integer, ServerSession> list = sessionMap.get(AppId.LOGIC);
//		if (list == null || list.size() == 0) {
//			return null;
//		}
//		return list.get(originalId);
//	}

	public ServerSession getMinLoadSession(AppId appId) {
		Map<Integer, ServerSession> list = sessionMap.get(appId);
		if (list == null || list.size() == 0) {
			return null;
		}
		List<ServerSession> sessionList = new ArrayList<>(list.values());
		Collections.sort(sessionList, (e, f) -> e.getLoadFactor() - f.getLoadFactor());
		return sessionList.get(0);
	}
	
	public ServerSession getNoStopMinLoadSession(AppId appId) {
		Map<Integer, ServerSession> list = sessionMap.get(appId);
		if (list == null || list.size() == 0) {
			return null;
		}
		List<ServerSession> sessionList = new ArrayList<>(list.values());
		Iterator<ServerSession> it = sessionList.iterator();
		while(it.hasNext()){
			if(it.next().getStop().get()){
				it.remove();
			}
		}
		if(sessionList.size() == 0) {
			return null;
		}
		Collections.sort(sessionList, (e, f) -> e.getLoadFactor() - f.getLoadFactor());
		return sessionList.get(0);
	}


	public ServerSession getServerSession(AppId appId, int serverId) {
		Map<Integer, ServerSession> map = sessionMap.get(appId);
		if (map == null) {
			return null;
		}
		return map.get(serverId);
	}
}
