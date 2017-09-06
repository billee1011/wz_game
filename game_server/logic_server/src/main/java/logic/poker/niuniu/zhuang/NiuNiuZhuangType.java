package logic.poker.niuniu.zhuang;

/**
 * Created by win7 on 2017/4/28.
 * 抢庄类型
 */
public enum NiuNiuZhuangType {
    GrabZhuang(1),      // 抢庄
    TrunZhuang(2),      // 轮庄
    OccupiedZhuang(3),; // 霸庄


    NiuNiuZhuangType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    private int value;

    public static NiuNiuZhuangType getByValue(int value) {
        for (NiuNiuZhuangType result : values()) {
            if (result.getValue() == value) {
                return result;
            }
        }
        return null;
    }
}
