package config.provider;

import conf.BaseProvider;
import conf.JsonUtil;
import config.bean.HeroBase;

import java.util.Map;

public class HeroInfoProvider extends BaseProvider {
    private static HeroInfoProvider ourInstance = new HeroInfoProvider();

    public static HeroInfoProvider getInst() {
        return ourInstance;
    }

    static {
        BaseProvider.providerList.add(ourInstance);
    }

    private Map<Integer, HeroBase> heroBaseMap = null;

    private HeroInfoProvider() {
    }

    public void loadConfig() {
        heroBaseMap = JsonUtil.getJsonMap(HeroBase[].class, "HeroBase.json");

        heroBaseMap.forEach((e, f) -> System.out.println(f));
    }

    private int bornHeroId = 10001;

    public HeroBase getBornMainHeroConfig() {
        return heroBaseMap.get(bornHeroId);
    }

}
