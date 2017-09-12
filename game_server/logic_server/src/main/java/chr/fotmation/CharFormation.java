package chr.fotmation;

import chr.RyCharacter;
import chr.hero.HeroEntity;

/**
 * Created by think on 2017/9/12.
 */
public class CharFormation {
	private RyCharacter ch;

	private Formation[] formations = new Formation[6];

	private long[] partners = new long[8];

	private long[] battleFormation = new long[6];

	public CharFormation(RyCharacter ch) {
		this.ch = ch;
	}

	public void addHeroIntoFormation(HeroEntity hero) {
		for (int i = 0, length = formations.length; i < length; i++) {
			if (formations[i] == null)
				formations[i] = new Formation(hero.getHeroId());
		}
	}

	public long calBattleScore() {
		long score = 0;
		for (Formation formation : formations) {
			if (formation == null)
				continue;
			HeroEntity hero = ch.getCharHero().getHeroEntity(formation.getHeroId());
			if (hero == null)
				continue;
			score += hero.calBattleScore();
		}
		return score;
	}
}
