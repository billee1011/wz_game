package config.provider;

import config.JsonUtil;
import config.bean.HeroBase;

import java.util.Map;

/**
 * Created by think on 2017/9/8.
 */
public class HeroInfoProvider extends BaseProvider {
	private static HeroInfoProvider instance = new HeroInfoProvider();

	private HeroInfoProvider() {

	}

	public static HeroInfoProvider getInst() {
		return instance;
	}

	private Map<Integer, HeroBase> heroBaseMap = null;

	{
		BaseProvider.providerList.add(this);
	}

	public HeroBase getHeroBaseConfById(int heroId) {
		return heroBaseMap.get(heroId);
	}

	@Override
	protected void initString() {

	}

	@Override
	public void doLoad() {
		heroBaseMap = JsonUtil.getJsonMap(HeroBase[].class, "HeroBase.json");

		heroBaseMap.forEach((e, f) -> System.out.println(f));
	}
}
