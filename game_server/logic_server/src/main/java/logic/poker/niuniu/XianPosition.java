package logic.poker.niuniu;

import java.util.Map;

import logic.poker.niuniu.zhuang.RoomNiuNiuDeskInfo;

/**
 * Created by Administrator on 2016/12/13.
 */
public enum XianPosition {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),;

    private int value;

    XianPosition(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static XianPosition getByValue(int value) {
        for (XianPosition position : values()) {
            if (position.getValue() == value) {
                return position;
            }
        }
        return null;
    }

    public static XianPosition getFree(Map<XianPosition, RoomNiuNiuDeskInfo> posInfoMap) {
        for (XianPosition position : values()) {
            if (!posInfoMap.keySet().contains(position)) {
                return position;
            }
        }
        return null;
    }
}
