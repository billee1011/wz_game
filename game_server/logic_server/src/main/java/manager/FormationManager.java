package manager;

import chr.RyCharacter;
import chr.fotmation.Formation;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.MessageLite;
import config.provider.HeroInfoProvider;
import inject.BeanManager;
import network.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangfang on 2017/9/15.
 */
@Singleton
public class FormationManager {
	private static Logger logger = LoggerFactory.getLogger(FormationManager.class);

	public static FormationManager getInst() {
		return BeanManager.getBean(FormationManager.class);
	}


	public void formationEquip(RyCharacter ch, MessageHolder<MessageLite> holder) {
		logger.info(" formation Equip  ");
	}

	public void addHeroToFormation(RyCharacter ch, MessageHolder<MessageLite> holder) {

	}

	public void changeHeroFromFormation(RyCharacter ch, MessageHolder<MessageHolder> holder) {

	}

	public void addHeroToPartners(RyCharacter ch, MessageHolder<MessageLite> holder) {

	}

}
