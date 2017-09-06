package config.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.JsonUtil;
import config.bean.ConfNiu;
import database.DataQueryResult;
import util.ASObject;

public class ConfNiuProvider extends BaseProvider{
	
	private static ConfNiuProvider ourInstance = new ConfNiuProvider();

	public static ConfNiuProvider getInst() {
		return ourInstance;
	}

	static {
		BaseProvider.providerList.add(ourInstance);
	}

	@Override
	protected void initString() {
		confString = JsonUtil.getGson().toJson(confNiuMap, Map.class);
	}

	@Override
	public void doLoad() {
		loadNiuResult();
	}
	
	private Map<Integer, ConfNiu> confNiuMap;
	
	public void loadNiuResult() {
		Map<Integer, ConfNiu> confNiuMap  = new HashMap<>();
		List<ASObject> listNiu = DataQueryResult.load("conf_niu", null);
		for(ASObject obj : listNiu){
			int roomId = obj.getInt("roomId");
			if(roomId == 0){
				continue;//客户端不需要
			}
			ConfNiu confNiu = new ConfNiu();
			
			confNiu.setBankCoin(obj.getInt("niuniu_bankMoney"));
			confNiu.setBannerLimit(obj.getInt("niuniu_bankCondition"));
			confNiu.setBannerTimes(obj.getInt("niuniu_bankTimes"));
			confNiu.setNiuniu_chip_limit(obj.getInt("niuniu_chip_limit"));
			confNiu.setNiuniu_small_chip(obj.getInt("niuniu_small_chip"));
			
			confNiuMap.put(roomId, confNiu);
		}
		
		this.confNiuMap = confNiuMap;
	}
	
	/**  0则通用 */
	public ConfNiu getConfNiu(int roomId){
		return confNiuMap.get(roomId);
	}
}
