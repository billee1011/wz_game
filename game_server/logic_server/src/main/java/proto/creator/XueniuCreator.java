package proto.creator;

import logic.majiong.PlayerDeskInfo;
import logic.majiong.XueniuFanType;
import logic.majiong.xueniu.XNOneCalRecord;
import proto.Xueniu;
import util.Pair;

import java.util.List;

/**
 * Created by Administrator on 2016/12/17.
 */
public class XueniuCreator {
	public static Xueniu.PBBeginChoseType createPBBeginChoseType(List<Integer> removeList, List<Integer> addList, int swapType) {
		Xueniu.PBBeginChoseType.Builder builder = Xueniu.PBBeginChoseType.newBuilder();
		if (removeList != null) {
			removeList.forEach(e -> builder.addRemove(e));
		}
		if (addList != null) {
			addList.forEach(e -> builder.addAdd(e));
		}
		builder.setSwapDirection(swapType);
		return builder.build();
	}

	public static Xueniu.PBCalculate createPBCalculate(int position, int type, List<Pair<Integer, Integer>> result, int huCard, int times) {
		return createPBCalculate(position, type, result, huCard, times, null);
	}

	public static Xueniu.PBCalculate createPBCalculate(int position, int type, List<Pair<Integer, Integer>> result, int huCard, int times, List<XueniuFanType> typeList) {
		Xueniu.PBCalculate.Builder builder = Xueniu.PBCalculate.newBuilder();
		builder.setPosition(position);
		builder.setType(type);
		result.forEach(e -> builder.addResult(CommonCreator.createPBPair(e.getLeft(), e.getRight())));
		builder.setCard(huCard);
		builder.setTimes(times);
		if (typeList != null) {
			for (XueniuFanType hu : typeList) {
				builder.addHuList(hu.getId());
			}
		}
		return builder.build();
	}


	public static Xueniu.PBOtherItem createPBOtherItem(int position, String name, String icon, int coin) {
		Xueniu.PBOtherItem.Builder builder = Xueniu.PBOtherItem.newBuilder();
		builder.setPosition(position);
		builder.setName(name);
		builder.setCoin(coin);
		builder.setIcon(icon);
		return builder.build();
	}

	public static Xueniu.PBCalItem createPBcalItem(int type, XNOneCalRecord record, int position) {
		Xueniu.PBCalItem.Builder builder = Xueniu.PBCalItem.newBuilder();
		builder.setType(type);
		builder.setPosition(record.getPosition());                    // need't
		builder.setCoin(record.getPositionGainLose(position));
		builder.setTimes(record.getTimes());
		if (record.getTypeList() != null) {
			record.getTypeList().forEach(e -> builder.addHuList(e.getId()));
		}
		return builder.build();
	}

	public static Xueniu.PBOtherCards createPBOtherCards(PlayerDeskInfo info) {
		Xueniu.PBOtherCards.Builder builder = Xueniu.PBOtherCards.newBuilder();
		builder.setPosition(info.getPosition().getValue());
		info.getHandCards().forEach(e -> builder.addCards(e));
		return builder.build();
	}

}
