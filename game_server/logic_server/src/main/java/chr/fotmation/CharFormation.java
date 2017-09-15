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

	public Formation[] getFormations() {
		return formations;
	}

	public void setFormations(Formation[] formations) {
		this.formations = formations;
	}

	public void setPartners(long[] partners) {
		this.partners = partners;
	}

	public void setBattleFormation(long[] battleFormation) {
		this.battleFormation = battleFormation;
	}

	public long[] getPartners() {
		return partners;
	}

	public long[] getBattleFormation() {
		return battleFormation;
	}

	public CharFormation(RyCharacter ch) {
		this.ch = ch;
	}

	public void addHeroIntoFormation(HeroEntity hero) {
		for (int i = 0, length = formations.length; i < length; i++) {
			if (formations[i] == null)
				formations[i] = new Formation(hero.getEntityId());
		}
		for (int i = 0, length = battleFormation.length; i < length; i++) {
			if (battleFormation[i] == 0)
				battleFormation[i] = hero.getEntityId();
		}
	}

	public void addFirstHero(HeroEntity hero) {
		formations[0] = new Formation(hero.getEntityId());
		battleFormation[1] = hero.getEntityId();
	}

	public long calBattleScore() {
		long score = 0;
		for (Formation formation : formations) {
			if (formation == null)
				continue;
			HeroEntity hero = ch.getCharHero().getEntity(formation.getHeroId());
			if (hero == null)
				continue;
			score += hero.calBattleScore();
		}
		return score;
	}
}
