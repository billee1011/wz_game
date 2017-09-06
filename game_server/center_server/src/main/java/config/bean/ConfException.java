package config.bean;

/**
 * Created by admin on 2017/3/28.
 */
public class ConfException {

    private int id;     /// 编号
    private long min_coin;  ///充值最小金额(分)
    private long max_coin;  ///充值最大金额(分)
    private float rate_lose_win;  ///输赢比

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getMin_coin() {
        return min_coin;
    }

    public void setMin_coin(long min_coin) {
        this.min_coin = min_coin;
    }

    public long getMax_coin() {
        return max_coin;
    }

    public void setMax_coin(long max_coin) {
        this.max_coin = max_coin;
    }

    public float getRate_lose_win() {
        return rate_lose_win;
    }

    public void setRate_lose_win(float rate_lose_win) {
        this.rate_lose_win = rate_lose_win;
    }

}
