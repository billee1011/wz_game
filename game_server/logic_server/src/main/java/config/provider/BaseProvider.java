package config.provider;

import inject.BeanManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangfang on 2016/9/12.
 */
public abstract class BaseProvider {

	public static String CONF_PATH = null;

	protected String confString;

	protected abstract void initString();

	protected static List<BaseProvider> providerList = new ArrayList<>();

	public void reLoad() {
		doLoad();
		initString();
	}

	public String getConfString() {
		return confString;
	}

	public static void init() {
		HeroInfoProvider.getInst();
		EquipInfoProvider.getInst();
	}

	public static void loadAll() {
		providerList.forEach(e -> e.loadConfig());
	}

	public boolean loadConfig() {
		doLoad();

		initString();
		return true;
	}


	public abstract void doLoad();
}
