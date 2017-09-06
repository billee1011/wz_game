package config.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.bean.ConfNiu;
import database.DataQueryResult;
import logic.poker.niuniu.NiuResult;
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
			Map<Integer, Integer>  niuResult = new HashMap<>();
			ConfNiu confNiu = new ConfNiu();
			niuResult.put(NiuResult.NO_NIU.getValue(), obj.getInt("noNiu"));
			niuResult.put(NiuResult.NIU_1.getValue(), obj.getInt("niu1"));
			niuResult.put(NiuResult.NIU_2.getValue(), obj.getInt("niu2"));
			niuResult.put(NiuResult.NIU_3.getValue(), obj.getInt("niu3"));
			niuResult.put(NiuResult.NIU_4.getValue(), obj.getInt("niu4"));
			niuResult.put(NiuResult.NIU_5.getValue(), obj.getInt("niu5"));
			niuResult.put(NiuResult.NIU_6.getValue(), obj.getInt("niu6"));
			niuResult.put(NiuResult.NIU_7.getValue(), obj.getInt("niu7"));
			niuResult.put(NiuResult.NIU_8.getValue(), obj.getInt("niu8"));
			niuResult.put(NiuResult.NIU_9.getValue(), obj.getInt("niu9"));
			niuResult.put(NiuResult.NIU_NIU.getValue(), obj.getInt("niuNiu"));
			niuResult.put(NiuResult.FIVE_NIU.getValue(), obj.getInt("fiveNiu"));
			niuResult.put(NiuResult.BOMB_NIU.getValue(), obj.getInt("bombNiu"));
			niuResult.put(NiuResult.FIVE_SMALL_NIU.getValue(), obj.getInt("fiveSmallNiu"));
			
			confNiu.setBankCoin(obj.getInt("niuniu_bankMoney"));
			confNiu.setBannerLimit(obj.getInt("niuniu_bankCondition"));
			confNiu.setBannerTimes(obj.getInt("niuniu_bankTimes"));
			confNiu.setNiuniuWinRate(obj.getInt("niuniu_bankWinRate"));
			confNiu.setNiuniu_chip_limit(obj.getInt("niuniu_chip_limit"));
			confNiu.setNiuniu_small_chip(obj.getInt("niuniu_small_chip"));
			confNiu.setNiuResultMap(niuResult);
			
			confNiuMap.put(obj.getInt("roomId"), confNiu);
		}
		
		this.confNiuMap = confNiuMap;
	}
	
	/**  0则通用 */
	public ConfNiu getConfNiu(int roomId){
		return confNiuMap.get(roomId);
	}
}
