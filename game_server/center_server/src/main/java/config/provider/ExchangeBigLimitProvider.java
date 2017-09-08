package config.provider;

import database.DataQueryResult;
import util.MapObject;

import java.util.List;

/**
 * Created by admin on 2017/4/6.
 */
public class ExchangeBigLimitProvider extends BaseProvider  {

    private static ExchangeBigLimitProvider inst = new ExchangeBigLimitProvider();

    private ExchangeBigLimitProvider() {

    }

    public static ExchangeBigLimitProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private long exchange_big_limit = 0;


    @Override
    protected void initString() {

    }

    @Override
    public void doLoad() {
        List<MapObject> confExchangeList = DataQueryResult.load("conf_exchange", null);
        if (confExchangeList.size() != 1) {
            return ;
        }
        this.exchange_big_limit = confExchangeList.get(0).getInt("param_big");
    }

    public long getBigLimit() {
        return this.exchange_big_limit;
    }

    public boolean isBigLimit(int value) {
        return this.exchange_big_limit < value;
    }
}
