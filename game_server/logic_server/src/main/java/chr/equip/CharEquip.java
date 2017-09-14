package chr.equip;

import base.EntityStorage;
import chr.RyCharacter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/14.
 */
public class CharEquip extends EntityStorage<EquipEntity> {
	private RyCharacter ch;

	public CharEquip(RyCharacter ch) {
		this.ch = ch;
	}

}