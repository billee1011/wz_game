package chr.attr;

import base.IEntity;
import config.bean.EBattleAttribute;
import define.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/14
 * 拥有属性的实体.
 */
public class AttrEntity extends IEntity {
	private Map<EBattleAttribute, Integer> attrMap;

	public AttrEntity(EntityType type) {
		super(type);
		attrMap = new HashMap<>();
	}


	public Map<EBattleAttribute, Integer> getAttrMap() {
		return attrMap;
	}

	public void addAttribute(EBattleAttribute type, int value) {
		attrMap.merge(type, value, (e, f) -> e == null ? f : e + f);
	}

	public void reduceAttribute(EBattleAttribute type, int value) {
		attrMap.merge(type, value, (e, f) -> e == null ? -f : e - f);
	}


}
