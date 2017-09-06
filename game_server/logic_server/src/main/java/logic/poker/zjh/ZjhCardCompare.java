package logic.poker.zjh;

import logic.poker.PokerCard;
import logic.poker.ddz.DdzGroupType;

import java.util.*;

/**
 * Created by hhhh on 2017/3/24.
 */
public class ZjhCardCompare {
	private static ZjhCardCompare instance = new ZjhCardCompare();

	private ZjhCardCompare() {
		init();
	}

	public static ZjhCardCompare getInst() {
		return instance;
	}

	interface ZjhSwich {
		ZjhCompareStrategy illegal(List<PokerCard> cards);
	}

	private Map<ZjhGroupType, ZjhSwich> zjhSwitchStrategy = new HashMap<>();

	private void initSwitch() {
		zjhSwitchStrategy.clear();
		zjhSwitchStrategy.put(ZjhGroupType.LEOPARD, ZjhSwitchStrategy::leopard);
		zjhSwitchStrategy.put(ZjhGroupType.STRAIGHTGOLD, ZjhSwitchStrategy::straightGold);
		zjhSwitchStrategy.put(ZjhGroupType.GOLDFLOWER, ZjhSwitchStrategy::goleFlower);
		zjhSwitchStrategy.put(ZjhGroupType.STRAIGHT, ZjhSwitchStrategy::stralght);
		zjhSwitchStrategy.put(ZjhGroupType.COUPLE, ZjhSwitchStrategy::couple);
		zjhSwitchStrategy.put(ZjhGroupType.SIGNLE, ZjhSwitchStrategy::signle);
		//特殊牌型不做处理
		zjhSwitchStrategy.put(ZjhGroupType.SPECIAL, ZjhSwitchStrategy::special);
	}

	private void init() {
		initSwitch();
	}

	/**
	 * 胜负状态
	 *
	 * @return 0 胜 1 负
	 */
	public static int compare(ZjhCompareStrategy a, ZjhCompareStrategy b, boolean hasSpecial) {
		int groupTypeA = a.getGroupType().getValue();
		int groupTypeB = b.getGroupType().getValue();
		
		if (!hasSpecial) { // 如果当前没有豹子 特殊牌型变为散牌型
			if (groupTypeA == ZjhGroupType.SPECIAL.getValue()) {
				groupTypeA = ZjhGroupType.SIGNLE.getValue();
			}
			if (groupTypeB == ZjhGroupType.SPECIAL.getValue()) {
				groupTypeB = ZjhGroupType.SIGNLE.getValue();
			}
		}
		
		//牌型相等
		if (groupTypeA == groupTypeB) {
			List<Integer> lista = a.getList();
			List<Integer> listb = b.getList();
			for (int i = 0; i < lista.size(); i++) {
				int cardA = lista.get(i);
				int cardB = listb.get(i);
				if (cardA > cardB) {
					return 0;
				} else if (cardA < cardB) {
					return 1;
				} else { // 如果 牌值一样 就比花色
					List<Integer> typeListA = a.getType();
					List<Integer> typeListB = b.getType();
					for (int j = 0; j < lista.size(); j++) {
						int typeA = typeListA.get(j);
						int typeB = typeListB.get(j);
						if (typeA > typeB) {
							return 0;
						} else {
							return 1;
						}
					}
				}
			}
			return 1;
		}
//		//特殊牌型
//		if (groupTypeA == 0 || groupTypeB == 0) {
//			if (groupTypeA == 0 && groupTypeB == ZjhGroupType.LEOPARD.getValue()) {
//				return 0;
//			}
//			if (groupTypeB == 0 && groupTypeA == ZjhGroupType.LEOPARD.getValue()) {
//				return 1;
//			}
//		}
		//牌型小的取胜
		if (groupTypeA < groupTypeB) {
			return 0;
		}
		return 1;
	}
	
	public static boolean hasLeopard(List<ZjhDeskInfo> infos) {
		for (ZjhDeskInfo info : infos) {
			if (info.getStrategy().getGroupType() == ZjhGroupType.LEOPARD) {
				return true;
			}
		}
		return false;
	}
	
	
	

	public ZjhCompareStrategy checkTypeAndValue(List<PokerCard> cardList) {
		for (ZjhGroupType type : ZjhGroupType.values()) {
			ZjhSwich handle = getInst().zjhSwitchStrategy.get(type);
			ZjhCompareStrategy strategy = handle.illegal(cardList);
			if (strategy != null) {
				return strategy;
			}
		}
		return null;
	}

}

