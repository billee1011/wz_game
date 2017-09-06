package config.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import config.AssisInfoProvider;
import config.CoupleRoomInfoProvider;
import config.DynamicInfoProvider;

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

	protected String confString;

	protected abstract void initString();

	protected static List<BaseProvider> providerList = new ArrayList<>();

	public static void init() {
		CoupleRoomInfoProvider.getInst();
		RankInfoProvider.getInst();
		DynamicInfoProvider.getInst();
		AssisInfoProvider.getInst();
		PersonalConfRoomProvider.getInst();
		ConfNiuProvider.getInst();
	}

	public void reLoad() {
		doLoad();
		initString();
	}

	public String getConfString() {
		return confString;
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
