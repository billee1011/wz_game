package logic.desk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import define.GameType;
import network.ServerSession;
import util.Randomizer;

/**
 * Created by Administrator on 2017/2/7.
 */
public class DeskManager {
	private static Logger logger = LoggerFactory.getLogger(DeskManager.class);
	private static DeskManager instance = new DeskManager();

	private DeskManager() {

	}

	public static DeskManager getInst() {
		return instance;
	}

	private Map<Integer, DeskInfo> allDesk = new ConcurrentHashMap<Integer, DeskInfo>();
	
	private Map<Integer,DeskInfo> niuNiuDeskMap = new ConcurrentHashMap<>();

//	public void addDesk(DeskInfo desk) {
//		allDesk.put(desk.getDeskId(), desk);
//	}

	public void removeDesk(DeskInfo desk) {
		if (desk != null) {
			if (desk.getPlayerList() != null) {
				desk.getPlayerList().forEach(e -> {
//					e.setRoomId(0);
					e.setDeskInfo(null);
				});
			}
			allDesk.remove(desk.getDeskId());
			if(niuNiuDeskMap != null && niuNiuDeskMap.containsValue(desk)){
				niuNiuDeskMap.remove(desk.getRoomId());
//				CenterServer.getInst().setNiuniuServerId(0);
			}
			desk.destroy();
			logger.info("删除桌子{},当前存在桌子数:{}", desk.getDeskId(), allDesk.size());
		}
	}

	public Collection<DeskInfo> getAllDesk() {
		return new ArrayList<DeskInfo>(allDesk.values());
	}

	public DeskInfo getDeskInfo(int deskId) {
		return allDesk.get(deskId);
	}

	/**
	 * 私房id
	 * @return
	 */
	public DeskInfo getPrivateDesk() {
		int deskId = 100000 + Randomizer.nextInt(900000);
		while (allDesk.get(deskId) != null) {
			deskId = 100000 + Randomizer.nextInt(900000);
		}
		DeskInfo desk = new DeskInfo(deskId);
		allDesk.put(desk.getDeskId(), desk);
		logger.info("创建桌子{},当前存在桌子数:{}",desk.getDeskId(),allDesk.size());
		return desk;
	}

	/**
	 * 非私房
	 */
	public  DeskInfo getNorPrivateDesk(GameType type){
		int deskId = 1000000 + Randomizer.nextInt(9000000);
		while (allDesk.get(deskId) != null) {
			deskId = 1000000 + Randomizer.nextInt(9000000);
		}
		DeskInfo d = null;
		if (type == null) {
			d = new DeskInfo(deskId);
		} else if (type == GameType.ZJH || type == GameType.GRAD_NIU || type == GameType.CLASS_NIU) {
			d = new GroupDesk(deskId);
		}
			
		d.setDeskId(deskId);
		allDesk.put(deskId, d);
		logger.info("创建桌子{},当前存在桌子数:{}",d.getDeskId(),allDesk.size());
		return d;
	}

	public DeskInfo getNiuniuDesk(int roomId) {
		return niuNiuDeskMap.get(roomId);
	}

	public void setNiuNiuDesk(DeskInfo niuNiuDesk) {
		niuNiuDeskMap.put(niuNiuDesk.getRoomId(), niuNiuDesk);
	}
	
	public ServerSession getNiuniuServer() {
		for(DeskInfo desk : niuNiuDeskMap.values()){
			return desk.getBindServerSession();
		}
		return null;
	}
	
	public void resetNiuNiuDeskMap(){
		niuNiuDeskMap.clear();
	}
	
}
