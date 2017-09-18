package manager;

import chr.RyCharacter;
import chr.equip.EquipEntity;
import chr.hero.HeroEntity;
import com.google.inject.Singleton;
import com.google.protobuf.MessageLite;
import inject.BeanManager;
import network.MessageHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proto.Common;

/**
 * Created by wangfang on 2017/9/15.
 */
@Singleton
public class EquipManager {
	private Logger logger = LoggerFactory.getLogger(EquipManager.class);


	public static EquipManager getInst() {
		return BeanManager.getBean(EquipManager.class);
	}


	public void wearEquipment(RyCharacter ch, MessageHolder<MessageLite> message) {

	}

	public void strengthenEquip(RyCharacter ch, MessageHolder<MessageLite> message) {
		Common.PBInt64 request = message.get();
		EquipEntity equipEntity = ch.getCharEquip().getEntity(request.getValue());
		if (equipEntity == null) {
			logger.warn("player {} strengthenEquip {} is not found", ch.getEntityId(), request.getValue());
			return;
		}
		equipEntity.setLevel(equipEntity.getLevel() + 1);

	}

	//service
	public HeroEntity getEquipHero(RyCharacter ch, EquipEntity entity) {
		return null;
	}
}
