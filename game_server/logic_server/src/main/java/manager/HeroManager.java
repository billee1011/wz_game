package manager;

import chr.RyCharacter;
import chr.hero.HeroEntity;
import com.google.inject.Singleton;
import com.google.protobuf.MessageLite;
import inject.BeanManager;
import network.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.Common;

/**
 * Created by think on 2017/9/18.
 */
@Singleton
public class HeroManager {
	private static Logger logger = LoggerFactory.getLogger(HeroManager.class);

	public static HeroManager getInst() {
		return BeanManager.getBean(HeroManager.class);
	}

	public void heroLevelUp(RyCharacter ch, MessageHolder<MessageLite> holder) {
		Common.PBInt64 request = holder.get();

		HeroEntity entity = ch.getCharHero().getEntity(request.getValue());

		if (entity == null) {
			logger.warn("unknow herp  player id {} hero id{]", ch.getEntityId(), request.getValue());
			return;
		}
		entity.setLevel(entity.getLevel() + 1);

		entity.reloadAttribute();

	}
}
