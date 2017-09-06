package logic.poker.niuniu.zhuang;

/**
 * Created by win7 on 2017/4/30.
 * 加注
 */
public enum RoomNiuNiuAddBetMultiple {
    DEFUALT(0,0),
    FIVE(5,0),
    SEVEN(7,1),
    TEN(10,0),
    TWELVE(12,1),
    FIFTEEN(15,0),
    TWENTY(20,2),
    TWENTY_FIVE(25,2),
    ;
    /**
     * 庄家确定后，非庄家玩家还可进行一次“下注”（庄家不能下注），既加额外倍数
     * 当玩家的剩余金币数小于等于  底分x庄家倍数x45 时，玩家可下注倍数选项为
     * A.5倍  B.7倍  C.10倍  D.12倍  E.15倍
     * 当玩家的剩余金币数大于  底分x庄家倍数x45时，玩家可下注倍数选项为
     * A.5倍  B.10倍  C.15倍  D.20倍  E.25倍
     * 同时当玩家的剩余金币数小于  底分x庄家倍数x3x该下注倍数  时，该下注倍数按
     * 钮为暗色不可点击状态
     */
    private int value;
    private int type;   // 0都适用, 1小于45倍，2大于45倍

    RoomNiuNiuAddBetMultiple(int value, int type) {
        this.value = value;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public static RoomNiuNiuAddBetMultiple getRoomNiuNiuAddBetMultipleByValue(int value) {
        for (RoomNiuNiuAddBetMultiple result : values()) {
            if (result.getValue() == value) {
                return result;
            }
        }
        return null;

    }
}
