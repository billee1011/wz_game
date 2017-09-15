package chr;

import base.IEntity;
import chr.equip.EquipEntity;
import chr.fotmation.CharFormation;
import chr.fotmation.Formation;
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
import java.util.*;

/**
 * Created by think on 2017/9/8.
 */
public class PlayerSaver {
	private static Logger logger = LoggerFactory.getLogger(PlayerSaver.class);


	public static void savePlayer(RyCharacter ch) {
		long chId = ch.getEntityId();
		saveModule(chId, DBAction.PLAYER, geneModuleData(chId, Arrays.asList(ch)));
		saveModule(chId, DBAction.HERO, geneModuleData(chId, ch.getCharHero().getAllEntities()));
		saveModule(chId, DBAction.EQUIP, geneModuleData(chId, ch.getCharEquip().getAllEntities()));
		saveModule(chId, DBAction.FORMATION, geneFormationModule(ch));
	}

	private static void saveModule(long playerId, DBAction action, MapObject data) {
		DataManager.getInst().saveModule(playerId, action, data);
	}


	private static <T extends IEntity> MapObject geneModuleData(long playerId, Collection<T> entityCollection) {
		MapObject moduleData = new MapObject();
		List<MapObject> dataList = new ArrayList<>();
		entityCollection.forEach(e -> dataList.add(geneData(playerId, e)));
		moduleData.put(playerId + "", dataList.toArray());
		return moduleData;
	}

	private static MapObject geneFormationModule(RyCharacter ch) {
		MapObject moduleData = new MapObject();
		List<MapObject> dataList = new ArrayList<>();
		CharFormation formation = ch.getCharFormation();
		MapObject data = new MapObject();
		data.put("player_id", ch.getEntityId());
		data.put("formation_info", JsonUtil.getGson().toJson(formation.getFormations(), Formation[].class));
		data.put("partners", JsonUtil.getGson().toJson(formation.getPartners(), long[].class));
		data.put("battle_formation", JsonUtil.getGson().toJson(formation.getBattleFormation(), long[].class));
		dataList.add(data);
		moduleData.put(ch.getEntityId() + "", dataList.toArray());
		return moduleData;
	}

	private static <T extends IEntity> MapObject geneData(long playerId, T entity) {
		switch (entity.getEntityType()) {
			case CHARACTER:
				return genePlayerData(playerId, (RyCharacter) entity);
			case HERO:
				return geneHeroData(playerId, (HeroEntity) entity);
			case EQUIP:
				return geneEquipData(playerId, (EquipEntity) entity);
			default:
				return null;
		}
	}

	private static MapObject geneHeroData(long playerId, HeroEntity entity) {
		MapObject data = new MapObject();
		data.put("player_id", playerId);
		data.put("hero_id", entity.getEntityId());
		data.put("conf_id", entity.getHeroId());
		data.put("level", entity.getLevel());
		data.put("exp", entity.getExp());
		data.put("break_level", entity.getBreakLevel());
		data.put("awake_level", entity.getAwakeLevel());
		data.put("awake_info", JsonUtil.getGson().toJson(entity.getAwakeInfo(), int[].class));
		data.put("tianming_level", entity.getTianmingLevel());
		return data;
	}

	private static MapObject genePlayerData(long playerId, RyCharacter ch) {
		MapObject data = new MapObject();
		data.put("player_id", ch.getEntityId());
		data.put("user_id", ch.getUserId());
		data.put("diamond", ch.getResourceManager().getResCount(EMoney.DIAMOND));
		data.put("silver", ch.getResourceManager().getResCount(EMoney.SILVER));
		data.put("reputation", ch.getResourceManager().getResCount(EMoney.REPUTATION));
		data.put("player_name", ch.getPlayerName());
		return data;
	}

	private static MapObject geneEquipData(long playerId, EquipEntity entity) {
		MapObject data = new MapObject();
		data.put("player_id", playerId);
		data.put("equip_id", entity.getEntityId());
		data.put("conf_id", entity.getEquipId());
		data.put("level", entity.getLevel());
		data.put("jinglian_level", entity.getJinglianLevel());
		data.put("jinglian_exp", entity.getJinglianExp());
		data.put("star_level", entity.getStarLevel());
		data.put("star_exp", entity.getStarExp());
		data.put("star_bless", entity.getStarBless());
		data.put("gold_level", entity.getGoldLevel());
		return data;
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
