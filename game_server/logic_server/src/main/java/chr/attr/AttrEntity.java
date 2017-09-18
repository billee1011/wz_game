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
	//含有百分比属性的时候的数值
	private Map<EBattleAttribute, Integer> attrMap;

	//百分比算成最终数值的数 ， 如果上面额修改了， 下面需要reload
	private Map<EBattleAttribute, Integer> finalAttr;

	public AttrEntity(EntityType type) {
		super(type);
		attrMap = new HashMap<>();
		finalAttr = new HashMap<>();
	}

	public Map<EBattleAttribute, Integer> getFinalAttr() {
		return finalAttr;
	}

	public Map<EBattleAttribute, Integer> getAttrMap() {
		return attrMap;
	}

	public void addAttribute(EBattleAttribute type, int value) {
		attrMap.merge(type, value, (e, f) -> e == null ? f : e + f);
	}

	public int getAttributeValue(EBattleAttribute type) {
		return attrMap.containsKey(type) ? attrMap.get(type) : 0;
	}

	public void reduceAttribute(EBattleAttribute type, int value) {
		attrMap.merge(type, value, (e, f) -> e == null ? -f : e - f);
	}

	public void clearAttribute() {
		this.attrMap.clear();
	}

	public void calFinalAttribute() {
		finalAttr.clear();
	}
}
