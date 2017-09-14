package chr.equip;

import base.IEntity;
import chr.attr.AttrEntity;
import config.bean.EBattleAttribute;
import config.bean.Equip;
import config.provider.EquipInfoProvider;
import define.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/14.
 */
public class EquipEntity extends AttrEntity {
	private int equipId;

	private int level;

	private int jinglianLevel;

	private int jinglianExp;

	private int starLevel;

	private int starExp;

	private int starBless;

	private int goldLevel;


	public EquipEntity() {
		super(EntityType.EQUIP);
		level = 1;
	}

	public int getEquipId() {
		return equipId;
	}

	public void setEquipId(int equipId) {
		this.equipId = equipId;
	}

	public int getGoldLevel() {
		return goldLevel;
	}

	public void setGoldLevel(int goldLevel) {
		this.goldLevel = goldLevel;
	}

	public int getStarLevel() {
		return starLevel;
	}

	public void setStarLevel(int starLevel) {
		this.starLevel = starLevel;
	}

	public int getStarExp() {
		return starExp;
	}

	public void setStarExp(int starExp) {
		this.starExp = starExp;
	}

	public int getStarBless() {
		return starBless;
	}

	public void setStarBless(int starBless) {
		this.starBless = starBless;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getJinglianLevel() {
		return jinglianLevel;
	}

	public void setJinglianLevel(int jinglianLevel) {
		this.jinglianLevel = jinglianLevel;
	}

	public int getJinglianExp() {
		return jinglianExp;
	}

	public void setJinglianExp(int jinglianExp) {
		this.jinglianExp = jinglianExp;
	}

	public static EquipEntity createEquip(long entityId, int confId) {
		EquipEntity entity = new EquipEntity();
		Equip conf = EquipInfoProvider.getInst().getEquipConfById(confId);

		return entity;
	}
}
