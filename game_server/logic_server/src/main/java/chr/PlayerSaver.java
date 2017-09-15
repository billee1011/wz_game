package chr;

import chr.equip.EquipEntity;
import chr.hero.CharHero;
import chr.hero.HeroEntity;
import config.JsonUtil;
import database.DBUtil;
import db.DataManager;
import db.data.DBAction;
import define.EMoney;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MapObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/9/8.
 */
public class PlayerSaver {
	private static Logger logger = LoggerFactory.getLogger(PlayerSaver.class);


	public static void savePlayer(RyCharacter ch) {
//		DataManager.getInst().saveModule(ch.getEntityId(), DBAction.PLAYER, genePlayerModule(ch));
		DataManager.getInst().saveModule(ch.getEntityId(), DBAction.EQUIP, geneEquipModule(ch));
	}

	private static MapObject genePlayerModule(RyCharacter ch) {
		MapObject data = new MapObject();
		return null;
	}

	private static MapObject geneEquipModule(RyCharacter ch) {
		MapObject moduleData = new MapObject();
		List<MapObject> dataList = new ArrayList<>();
		ch.getCharEquip().getEntityMap().entrySet().forEach(e -> {
			MapObject data = new MapObject();
			data.put("player_id", e.getKey());
			EquipEntity entity = e.getValue();
			data.put("equip_id", entity.getEntityId());
			data.put("conf_id", entity.getEquipId());
			data.put("level", entity.getLevel());
			data.put("jinglian_level", entity.getJinglianLevel());
			data.put("jinglian_exp", entity.getJinglianExp());
			data.put("star_level", entity.getStarLevel());
			data.put("star_exp", entity.getStarExp());
			data.put("star_bless", entity.getStarBless());
			data.put("gold_level", entity.getGoldLevel());
			dataList.add(data);
		});
		moduleData.put(ch.getEntityId() + "", dataList.toArray());
		return moduleData;
	}


	public static long insertPlayer(RyCharacter ch) throws SQLException {
		Map<String, Object> data = new HashMap<>();
		data.put("player_id", ch.getEntityId());
		data.put("diamond", ch.getResourceManager().getResCount(EMoney.DIAMOND));
		data.put("silver", ch.getResourceManager().getResCount(EMoney.SILVER));
		data.put("reputation", ch.getResourceManager().getResCount(EMoney.REPUTATION));
		data.put("player_name", ch.getPlayerName());
		data.put("user_id", ch.getUserId());
		return DBUtil.executeInsert("player", data);
	}
}
