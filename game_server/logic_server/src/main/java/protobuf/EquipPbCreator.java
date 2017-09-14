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
		entity.getAttrMap().entrySet().forEach(e -> builder.putAttts(e.getKey().getValue(), e.getValue()));
		return builder.build();
	}
}
