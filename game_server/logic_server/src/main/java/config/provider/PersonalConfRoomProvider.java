package config.provider;

import config.JsonUtil;
import config.bean.PersonalConfRoom;
import config.bean.Rank;
import database.DataQueryResult;
import util.ASObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/9.
 */
public class PersonalConfRoomProvider extends BaseProvider {
	private static PersonalConfRoomProvider inst = new PersonalConfRoomProvider();

	private PersonalConfRoomProvider() {

	}

	public static PersonalConfRoomProvider getInst() {
		return inst;
	}

	static {
		BaseProvider.providerList.add(inst);
	}

	private Map<Integer, PersonalConfRoom> pConfRoomMap = null;

	@Override
	public void doLoad() {
		loadPerConfRoomInfo();
	}


	private void loadPerConfRoomInfo() {
		Map<Integer, PersonalConfRoom> pConfRoomMap = new HashMap<>();
		List<ASObject> confRoomList = DataQueryResult.load("select * from conf_personal_room");
		for (ASObject info : confRoomList) {
			PersonalConfRoom p_conf_room = new PersonalConfRoom();
			p_conf_room.setMode(info.getInt("mode"));
			p_conf_room.setTax_rate(info.getInt("tax_rate"));

			pConfRoomMap.put(info.getInt("mode"), p_conf_room);
		}
		this.pConfRoomMap = pConfRoomMap;
	}

	public int getPersonalConfRoom(int mode) {
		for (Map.Entry<Integer, PersonalConfRoom> info : pConfRoomMap.entrySet()) {
			if (info.getKey() == mode) {
				return info.getValue().getTax_rate();
			}
		}
		return 1;
	}

	public PersonalConfRoom getPersonalConfRoomById(int mode){
		return pConfRoomMap.get(mode);
	}

	@Override
	protected void initString() {
		confString = JsonUtil.getGson().toJson(pConfRoomMap, Map.class);
	}
}
