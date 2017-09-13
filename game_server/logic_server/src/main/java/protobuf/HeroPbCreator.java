package protobuf;

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
        hero.getAttributeMap().entrySet().forEach(e -> {
            builder.putAttributes(e.getKey().getValue(), e.getValue());
        });
        return builder.build();
    }
}
