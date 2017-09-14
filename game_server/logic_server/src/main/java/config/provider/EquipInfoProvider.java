package config.provider;

import config.JsonUtil;
import config.bean.Equip;

import java.util.Map;

/**
 * Created by think on 2017/9/14.
 */
public class EquipInfoProvider extends BaseProvider {

	private static EquipInfoProvider instance = new EquipInfoProvider();

	private EquipInfoProvider() {

	}

	public static EquipInfoProvider getInst() {
		return instance;
	}

	static {
		providerList.add(instance);
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
}
