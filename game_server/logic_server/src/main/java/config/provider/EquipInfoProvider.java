package config.provider;

import com.google.inject.Singleton;
import config.JsonUtil;
import config.bean.Equip;
import inject.BeanManager;

import java.util.Map;

/**
 * Created by think on 2017/9/14.
 */
@Singleton
public class EquipInfoProvider extends BaseProvider {

	public static EquipInfoProvider getInst() {
		return BeanManager.getBean(EquipInfoProvider.class);
	}

	{
		providerList.add(this);
	}

	private Map<Integer, Equip> equipMap = null;

	@Override
	protected void initString() {

	}

	public Equip getEquipConfById(int id) {
		return equipMap.get(id);
	}

	@Override
	public void doLoad() {
		equipMap = JsonUtil.getJsonMap(Equip[].class, "equip.json");

		equipMap.values().forEach(System.out::println);
	}

	@Override
	public String toString() {
		return JsonUtil.getGson().toJson(equipMap);
	}
}
