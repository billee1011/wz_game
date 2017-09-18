package chr;

import base.EntityCreator;
import chr.equip.EquipEntity;
import chr.fotmation.Formation;
import chr.hero.HeroEntity;
import config.JsonUtil;
import db.CharData;
import db.data.DBAction;
import define.EMoney;
import util.MapObject;

/**
 * Created by think on 2017/9/8.
 */
public class PlayerLoader {

	public static RyCharacter loadFromCharData(CharData data) {
		RyCharacter ch = RyCharacter.getEmptyChar(data.getCharId());
		loadBaseData(ch, data);
		loadHeroEntity(ch, data);
		loadEquipData(ch, data);
		loadFormation(ch, data.getModuleData(DBAction.FORMATION));
		ch.reloadAllItemAttribute();
		return ch;
	}

	private static void loadFormation(RyCharacter ch, MapObject moduleData) {
		Object[] datas = (Object[]) moduleData.get("" + ch.getEntityId());
		if (datas.length == 0)
			return;
		MapObject data = (MapObject) datas[0];
		ch.getCharFormation().setPartners(JsonUtil.getGson().fromJson(data.getString("partners"), long[].class));
		ch.getCharFormation().setBattleFormation(JsonUtil.getGson().fromJson(data.getString("battle_formation"), long[].class));
		ch.getCharFormation().setFormations(JsonUtil.getGson().fromJson(data.getString("formation_info"), Formation[].class));
	}

	private static void loadBaseData(RyCharacter ch, CharData charData) {
		MapObject baseData = charData.getModuleData(DBAction.PLAYER);
		Object[] datas = (Object[]) baseData.get("" + ch.getEntityId());
		MapObject playerData = (MapObject) datas[0];
		ch.setPlayerName(playerData.getString("player_name"));
		ch.setUserId(playerData.getInt("user_id"));
		ch.getResourceManager().updateResource(EMoney.DIAMOND, playerData.getLong("diamond"), true);
		ch.getResourceManager().updateResource(EMoney.SILVER, playerData.getLong("silver"), true);
		ch.getResourceManager().updateResource(EMoney.REPUTATION, playerData.getLong("reputation"), true);
	}

	private static void loadEquipData(RyCharacter ch, CharData charData) {
		MapObject baseData = charData.getModuleData(DBAction.EQUIP);
		Object[] datas = (Object[]) baseData.get("" + ch.getEntityId());
		for (Object data : datas) {
			MapObject equipData = (MapObject) data;
			EquipEntity equipEntity = EquipEntity.getEmptyEntity();
			equipEntity.setEntityId(equipData.getLong("equip_id"));
			equipEntity.setEquipId(equipData.getInt("conf_id"));
			equipEntity.setLevel(equipData.getInt("level"));
			equipEntity.setJinglianExp(equipData.getInt("jinglian_exp"));
			equipEntity.setJinglianLevel(equipData.getInt("jinglian_level"));
			equipEntity.setStarLevel(equipData.getInt("star_level"));
			equipEntity.setStarExp(equipData.getInt("star_exp"));
			equipEntity.setStarBless(equipData.getInt("star_bless"));
			equipEntity.setGoldLevel(equipData.getInt("gold_level"));
			ch.getCharEquip().addEntity(equipEntity);
		}
	}


	//怎么读取都行啊， 反正都话
	private static void loadHeroEntity(RyCharacter ch, CharData charData) {
		MapObject baseData = charData.getModuleData(DBAction.HERO);
		Object[] datas = (Object[]) baseData.get("" + ch.getEntityId());
		for (Object data : datas) {
			MapObject heroData = (MapObject) data;
			HeroEntity heroEntity = HeroEntity.getEmptyEntity();
			heroEntity.setEntityId(heroData.getLong("hero_id"));
			heroEntity.setHeroId(heroData.getInt("conf_id"));
			heroEntity.setAwakeLevel(heroData.getInt("awake_level"));
			heroEntity.setTianmingLevel(heroData.getInt("tianming_level"));
			heroEntity.setBreakLevel(heroData.getInt("break_level"));
			heroEntity.setLevel(heroData.getInt("level"));
			heroEntity.setExp(heroData.getInt("exp"));
			heroEntity.setAwakeInfo(JsonUtil.getGson().fromJson(heroData.getString("awake_info"), int[].class));
			ch.getCharHero().addEntity(heroEntity);
		}
	}
}
