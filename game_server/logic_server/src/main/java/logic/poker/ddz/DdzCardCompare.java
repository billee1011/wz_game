package logic.poker.ddz;

import logic.poker.PokerCard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/14.
 */
public class DdzCardCompare {
    private static DdzCardCompare instance = new DdzCardCompare();

    private DdzCardCompare() {
        init();
    }

    private static DdzCardCompare getInst() {
        return instance;
    }

    interface DdzComparator {
        boolean isBigger(List<Integer> src, List<Integer> target);
    }

    interface DdzSwitch {
        List<Integer> illegal(List<PokerCard> cards);
    }

    private Map<DdzGroupType, DdzSwitch> ddzSwitchStrategy = new HashMap<>();

    private Map<DdzGroupType, DdzComparator> ddzCompareStrategy = new HashMap<>();


    private void initComparator() {
        ddzCompareStrategy.clear();
        for (DdzGroupType type : DdzGroupType.values()) {
            if (type == DdzGroupType.JOKER_BONUS) {
                ddzCompareStrategy.put(type, DdzCompareStrategy::jokerBonus);
            } else {
                ddzCompareStrategy.put(type, DdzCompareStrategy::single);
            }
        }
    }

    private void initSwitch() {
        ddzSwitchStrategy.clear();
        ddzSwitchStrategy.put(DdzGroupType.SINGLE, DdzSwitchStrategy::single);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE, DdzSwitchStrategy::couple);
        ddzSwitchStrategy.put(DdzGroupType.TRIPLE, DdzSwitchStrategy::triple);
        ddzSwitchStrategy.put(DdzGroupType.FOUR, DdzSwitchStrategy::four);
        ddzSwitchStrategy.put(DdzGroupType.THREE_ONE, DdzSwitchStrategy::threeOne);
        ddzSwitchStrategy.put(DdzGroupType.THREE_TWO, DdzSwitchStrategy::threeTwo);
        ddzSwitchStrategy.put(DdzGroupType.FOUR_TWO_ONE, DdzSwitchStrategy::fourTwoOne);
        ddzSwitchStrategy.put(DdzGroupType.FOUR_TWO_TWO, DdzSwitchStrategy::fourTwoTwo);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_5, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_6, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_7, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_8, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_9, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_10, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_11, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.STRAIGHT_12, DdzSwitchStrategy::straight5);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_2_0, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_2_1, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_2_2, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_3_0, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_3_1, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_3_2, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_4_0, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_4_1, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_4_2, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_5_0, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_5_0, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_5_1, DdzSwitchStrategy::planeWithOne);
        ddzSwitchStrategy.put(DdzGroupType.PLANE_6_0, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_THREE, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_FOUR, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_FIVE, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_SIX, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_SEVEN, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_EIGHT, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_NINE, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.COUPLE_TEN, DdzSwitchStrategy::coupleThree);
        ddzSwitchStrategy.put(DdzGroupType.JOKER_BONUS, DdzSwitchStrategy::jokerBonus);
    }

    private void init() {
        initComparator();
        initSwitch();
    }

    public static boolean isBigger(DdzGroupType type, List<Integer> list1, List<Integer> list2) {
        DdzComparator handle = getInst().ddzCompareStrategy.get(type);
        if (handle == null) {
            return false;
        }
        return handle.isBigger(list1, list2);
    }

    public static List<Integer> checkType(DdzGroupType type, List<PokerCard> cardList) {
        DdzSwitch handle = getInst().ddzSwitchStrategy.get(type);
        if (handle == null) {
            return null;
        }
        return handle.illegal(cardList);
    }
}
