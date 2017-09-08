package chr.hero;

import chr.RyCharacter;
import util.ArrayMap;

import java.util.*;

/**
 * Created by think on 2017/9/8.
 */
public class CharHero {
	private RyCharacter character = null;

	private Map<Long, HeroEntity> heroMap;

	public CharHero(RyCharacter character) {
		this.character = character;
		heroMap = new HashMap<>();                                 //key => value very fast , haha
	}

	public Map<Long, HeroEntity> getHeroMap() {
		return heroMap;
	}

	public void addHero(HeroEntity entity) {
		heroMap.put(entity.getEntityId(), entity);
	}

	public void removeHero(long entityId) {
		heroMap.remove(entityId);
	}

	//你属于我， 我属于你
}
