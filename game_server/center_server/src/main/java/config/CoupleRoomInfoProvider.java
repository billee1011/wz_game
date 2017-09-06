package config;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.bean.CoupleRoom;
import config.bean.GrabNiuConfig;
import config.bean.RoomConfig;
import config.provider.BaseProvider;
import database.DBUtil;
import database.DataQueryResult;
import define.GameType;
import util.ASObject;

public class CoupleRoomInfoProvider extends BaseProvider {
	private static CoupleRoomInfoProvider ourInstance = new CoupleRoomInfoProvider();

	public static CoupleRoomInfoProvider getInst() {
		return ourInstance;
	}

	static {
		BaseProvider.providerList.add(ourInstance);
	}

	private Map<Integer, CoupleRoom> coupleRoomCfgMap = new HashMap<>();
	private Map<Integer, RoomConfig> conf_room_ex = new HashMap<>();
	private String confGrabNiu = "";

	private CoupleRoomInfoProvider() {
	}

	public void doLoad() {
		loadConfRoom();
		loadConfRoomEx();
		loadConfGrabNiu();
	}

	public void loadConfRoom() {
		List<ASObject> roomList = DataQueryResult.load("conf_room", null);
		Map<Integer, CoupleRoom> coupleRoomCfgMap = new HashMap<>();
		roomList.forEach(e -> coupleRoomCfgMap.put(e.getInt("id"), new CoupleRoom(e)));
		this.coupleRoomCfgMap = coupleRoomCfgMap;
	}

	public void loadConfRoomEx() {
		List<ASObject> roomList = DataQueryResult.load("conf_room_ex", null);
		Map<Integer, RoomConfig> conf_room_ex = new HashMap<>();
		roomList.forEach(e -> conf_room_ex.put(e.getInt("id"), new RoomConfig(e)));
		this.conf_room_ex = conf_room_ex;
	}
	
	public void loadConfGrabNiu() {
		Map<Integer, GrabNiuConfig> conf_grab_niu = new HashMap<>();
		List<ASObject> grab_niu_List = DataQueryResult.load("conf_grab_niu", null);
		grab_niu_List.forEach(e -> conf_grab_niu.put(e.getInt("room_id"), new GrabNiuConfig(e)));
		this.confGrabNiu = JsonUtil.getGson().toJson(conf_grab_niu, Map.class);
	}

	private void saveToDataBase(CoupleRoom conf) {
		Map<String, Object> data = new HashMap<>();
		data.put("id", conf.getId());
		data.put("lowScore", getBaseScoreOfRoom(conf.getId()));
		data.put("startValue", conf.getMinReq());
		data.put("endValue", conf.getMaxReq());
		data.put("descript", conf.getLimitStr());
		data.put("mode", conf.getMode());
		data.put("order1", conf.getOrder());
		data.put("btnBg", conf.getBtnBg());
		data.put("classify", conf.getClassify());
		data.put("coin_icon", conf.getCoinIcon());
		data.put("people_icon", conf.getPeopleIcon());
		data.put("tax_rate", conf.getTax_rate());
		try {
			DBUtil.executeInsert("conf_room", data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public Map<Integer, CoupleRoom> getCoupleRoomCfgMap(GameType game) {
		Map<Integer, CoupleRoom> result = new HashMap<>();
		for (CoupleRoom room : coupleRoomCfgMap.values()) {
			if (room.getMode() == game.getValue()) {
				result.put(room.getId(), room);
			}
		}
		return result;
	}

	public int getRoomStatus(int id) {
		CoupleRoom room = coupleRoomCfgMap.get(id);
		if(null != room) {
			return room.getStatus();
		}
		return 0;
	}

	@Override
	protected void initString() {
		confString = JsonUtil.getGson().toJson(coupleRoomCfgMap, Map.class);
	}

	public CoupleRoom getRoomConf(int id) {
		return coupleRoomCfgMap.get(id);
	}

	public int getBaseScoreOfRoom(int roomId) {
		CoupleRoom conf = coupleRoomCfgMap.get(roomId);
		return conf == null ? 0 : (int) (conf.getBase() * 100);
	}

	public int getFastRoomId(long coin) {
		CoupleRoom result = null;
		long minValue = Long.MIN_VALUE;
		for (CoupleRoom conf : coupleRoomCfgMap.values()) {
			if (coin - conf.getMinReq() < 0) {
				continue;
			}
			if (result == null || (coin - conf.getMinReq()) < minValue) {
				minValue = coin - conf.getMinReq();
				result = conf;
			}
		}
		return result == null ? -1 : result.getId();
	}

	public RoomConfig getConfRoomEx(int room_id) {
		for(Map.Entry<Integer, RoomConfig> entry : conf_room_ex.entrySet()) {
			if(entry.getValue().getRoom_id() == room_id) {
				return  entry.getValue();
			}
		}

		return null;
	}
	
	public String getGrabNiuConf() {
		return confGrabNiu;
	}
}
