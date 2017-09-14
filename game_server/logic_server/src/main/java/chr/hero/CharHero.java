package chr.hero;

import base.EntityStorage;
import chr.RyCharacter;
import util.ArrayMap;

import java.util.*;

/**
 * Created by think on 2017/9/8.
 */
public class CharHero extends EntityStorage<HeroEntity> {
	private RyCharacter character = null;


	public CharHero(RyCharacter character) {
		super();
		this.character = character;
	}
	//你属于我， 我属于你
}
