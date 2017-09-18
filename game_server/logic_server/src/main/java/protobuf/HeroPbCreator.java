package protobuf;

import chr.RyCharacter;
import chr.fotmation.Formation;
import chr.hero.HeroEntity;
import proto.Hero;

/**
 * Created by think on 2017/9/13.
 */
public class HeroPbCreator {

	public static Hero.PBHeroEntity heroEntity(HeroEntity hero) {
		Hero.PBHeroEntity.Builder builder = Hero.PBHeroEntity.newBuilder();
		builder.setHeroId(hero.getEntityId());
		builder.setConfId(hero.getHeroId());
		builder.setLevel(hero.getLevel());
		builder.setExp(hero.getExp());
		builder.setBreakLevel(hero.getBreakLevel());
		for (int i : hero.getAwakeInfo()) {
			builder.addAwakeInfo(i);
		}
		builder.setAwakeLevel(hero.getAwakeLevel());
		builder.setTianmingLevel(hero.getTianmingLevel());
		hero.getAttrMap().entrySet().forEach(e -> {
			builder.putAttributes(e.getKey().getValue(), e.getValue());
		});
		return builder.build();
	}

	public static Hero.PBFormation formation(RyCharacter ch) {
		Hero.PBFormation.Builder builder = Hero.PBFormation.newBuilder();
		for (Formation formation : ch.getCharFormation().getFormations()) {
			if (formation != null)
				builder.addFormation(oneFormation(formation));
		}
		for (long id : ch.getCharFormation().getPartners()) {
			builder.addPartner(id);
		}
		for (long id : ch.getCharFormation().getBattleFormation()) {
			builder.addBattleFormation(id);
		}
		return builder.build();
	}

	public static Hero.PBOneFormation oneFormation(Formation formation) {
		Hero.PBOneFormation.Builder builder = Hero.PBOneFormation.newBuilder();
		builder.setHeroId(formation.getHeroId());
		for (long equipId : formation.getEquipId()) {
			builder.addEquip(equipId);
		}
		builder.setPetId(formation.getPetId());
		builder.setHorseId(formation.getHorseId());
		builder.setMingjiangId(formation.getMingjiangId());
		return builder.build();
	}
}
