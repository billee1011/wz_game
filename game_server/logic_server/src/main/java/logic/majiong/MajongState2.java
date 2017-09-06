package logic.majiong;

import java.util.ArrayList;
import java.util.List;

public enum MajongState2 {
    UN_KNOW(),
    BEGIN(),            // 开始
    MO(),               // 摸牌
    CHU(),              // 出牌
    TING(),             // 听牌
    HU(),               // 胡牌
    GANG(),             // 杠牌
    PENG(),             // 碰牌
    CHI(),              // 吃牌
    GUO(),;             // 过牌


    private int value;

    public int getValue() {
        return value;
    }


}
