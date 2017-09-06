package logic.poker.niuniu.zhuang;

/**
 * Created by win7 on 2017/4/29.
 * 抢庄倍数
 */
public enum ZhuangMultiple {
    DEFAULT(-1,1),      // 默认
    NONE(0, 1),        // 不抢
    ONE(1, 1),         // 1倍
    TWO(2, 2),         // 2倍
    THREE(3, 3),       // 3倍
    FOUR(4,4),         // 4倍
    ;
    private int type;
    private int value;

    ZhuangMultiple(int type, int value) {
        this.type = type;
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public static ZhuangMultiple getByType(int type) {
        for (ZhuangMultiple result : values()) {
            if (result.getType() == type) {
                return result;
            }
        }
        return null;
    }
}
