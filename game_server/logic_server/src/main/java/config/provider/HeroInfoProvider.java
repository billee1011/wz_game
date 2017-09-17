package config.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import config.JsonUtil;
import config.bean.HeroBase;
import config.bean.Hero_union;
import inject.BeanManager;

import java.util.Map;

/**
 * Created by think on 2017/9/8.
 */
@Singleton
public class HeroInfoProvider extends BaseProvider {

	public static HeroInfoProvider getInst() {
		return BeanManager.getBean(HeroInfoProvider.class);
	}

	private Map<Integer, HeroBase> heroBaseMap = null;

	private Map<Integer, Hero_union> heroUnionMap = null;

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
		heroUnionMap = JsonUtil.getJsonMap(Hero_union[].class, "Hero_union.json");

	}

	@Override
	public String toString() {
		return JsonUtil.getGson().toJson(heroUnionMap);
	}
}
