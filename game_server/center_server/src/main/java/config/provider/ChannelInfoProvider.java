package config.provider;

import config.bean.ChannelConfig;
import database.DataQueryResult;
import service.CenterServer;
import util.ASObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/3/5.
 */
public class ChannelInfoProvider extends BaseProvider {

	private static ChannelInfoProvider instance = new ChannelInfoProvider();

	private ChannelInfoProvider() {

	}

	static {
		BaseProvider.providerList.add(instance);
	}

	public static ChannelInfoProvider getInst() {
		return instance;
	}

	private Map<Integer, Map<Integer, ChannelConfig>> channelConfigMap = new HashMap<>();

	public ChannelConfig getChannelConfig(int channelId, int packageId, boolean isReview) {
		if (isReview){ //  如果是审核版本  采用审核版本配置
			channelId = CenterServer.getInst().getReviewChannel();
			packageId = CenterServer.getInst().getReviewPackage();
		}
		Map<Integer, ChannelConfig> packageMap = channelConfigMap.get(channelId);
		if (packageMap == null) {
			return new ChannelConfig();
		}
		ChannelConfig result = packageMap.get(packageId);
		if (result == null) {
			result = new ChannelConfig();
		}
		return result;
	}


	@Override
	protected void initString() {

	}

	@Override
	public void doLoad() {
		Map<Integer, Map<Integer, ChannelConfig>> channelConfigMap = new HashMap<>();
		List<ASObject> roomList = DataQueryResult.load("conf_channel_switch", null);
		roomList.forEach(e -> {
			Map<Integer, ChannelConfig> packageMap = channelConfigMap.get(e.getInt("id"));
			if (packageMap == null) {
				packageMap = new HashMap<>();
				channelConfigMap.put(e.getInt("id"), packageMap);
			}
			packageMap.put(e.getInt("package_id"), new ChannelConfig(e));
		});
		
		this.channelConfigMap = channelConfigMap;
	}
}
