package protobuf;

import chr.equip.EquipEntity;
import proto.Equip;

/**
 * Created by think on 2017/9/14.
 */
public class EquipPbCreator {

	public static Equip.PBEquipEntity equipEntity(EquipEntity entity) {
		Equip.PBEquipEntity.Builder builder = Equip.PBEquipEntity.newBuilder();
		builder.setEntityId(entity.getEntityId());
		builder.setConfId(entity.getEquipId());
		builder.setLevel(entity.getLevel());
		builder.setGoldLevel(entity.getGoldLevel());
		builder.setStarBless(entity.getStarBless());
		builder.setStarLevel(entity.getStarLevel());
		builder.setStarExp(entity.getStarExp());
		builder.setJinglianLevel(entity.getJinglianLevel());
		builder.setJinglianExp(entity.getJinglianExp());
		entity.getAttrMap().entrySet().forEach(e -> builder.putAttts(e.getKey().getValue(), e.getValue()));
		return builder.build();
	}
}
