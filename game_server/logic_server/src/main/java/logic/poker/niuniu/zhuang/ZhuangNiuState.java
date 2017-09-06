package logic.poker.niuniu.zhuang;

/**
 * Created by win7 on 2017/4/29.
 * 房间状态
 */
public enum ZhuangNiuState {
    WATT(0,3),                   // 等待
    PRE_REDAY(1,0),              // 预准备
    DEAL_CARD1(2, 0),            // 发牌阶段1
    GRAB_ZHUANG(3, 5),           // 抢庄阶段
    BET(4, 10),                  // 下注阶段
    DEAL_CARD2(5,0),             // 发牌阶段2
    PLAYER_CAL(6, 10),           // 玩家算牌
    CALCULATE(7, 10),            // 玩家开牌结算
    ;

    ZhuangNiuState(int id) {
        this(id, -1);
    }

    ZhuangNiuState(int id, int second) {
        this.id = id;
        this.second = second;
    }

    private int id;
    private int second;

    public int getId() {
        return id;
    }

    public int getSecond() {
        return second;
    }
}
