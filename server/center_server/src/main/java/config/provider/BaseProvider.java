package config.provider;

import config.CoupleRoomInfoProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangfang on 2016/9/12.
 */
public abstract class BaseProvider {

	public static String CONF_PATH = null;

	static {
		StringBuilder path = new StringBuilder(100);
		path.append(System.getProperty("user.dir"));
		path.append(File.separator);
		path.append("conf" + File.separator);
		String confPath = path.toString();
		CONF_PATH = confPath;
	}


	protected static List<BaseProvider> providerList = new ArrayList<>();

	public static void init() {
		CoupleRoomInfoProvider.getInstance();
		RankInfoProvider.getInst();
	}

	public static void loadAll() {
		providerList.forEach(e -> e.loadConfig());
	}

	public abstract void loadConfig();
}
