package config;

import config.bean.CoupleRoom;
import config.provider.BaseProvider;

import java.util.HashMap;
import java.util.Map;

public class CoupleRoomInfoProvider extends BaseProvider {
	private static CoupleRoomInfoProvider ourInstance = new CoupleRoomInfoProvider();

	public static CoupleRoomInfoProvider getInstance() {
		return ourInstance;
	}

	static {
		BaseProvider.providerList.add(ourInstance);
	}

	private Map<Integer, CoupleRoom> coupleRoomCfgMap = null;

	private CoupleRoomInfoProvider() {
	}

	public void loadConfig() {
		coupleRoomCfgMap = JsonUtil.getJsonMap(CoupleRoom[].class, "couple_room.json");
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

}
