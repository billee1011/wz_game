package config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.bean.AssisSeriation;
import config.provider.BaseProvider;
import database.DataQueryResult;
import util.ASObject;

public class AssisInfoProvider extends BaseProvider{
	
	private static AssisInfoProvider ourInstance = new AssisInfoProvider();

	public static AssisInfoProvider getInst() {
		return ourInstance;
	}

	static {
		BaseProvider.providerList.add(ourInstance);
	}
	
	
	private Map<Integer, AssisSeriation> AssisSeriationMap = new HashMap<Integer, AssisSeriation>();

	@Override
	protected void initString() {
		confString = JsonUtil.getGson().toJson(AssisSeriationMap, Map.class);
	}

	@Override
	public void doLoad() {
		loadAssisSeriation();
	}

	public void loadAssisSeriation() {
		Map<Integer, AssisSeriation> AssisSeriationMap = new HashMap<Integer, AssisSeriation>();
		List<ASObject> seriationList = DataQueryResult.load("assis_seriation", null);
		seriationList.forEach(e -> AssisSeriationMap.put(e.getInt("game_type"), new AssisSeriation(e)));
		this.AssisSeriationMap = AssisSeriationMap;
	}

	public AssisSeriation getAssisSeriation(int gameType) {
		return AssisSeriationMap.get(gameType);
	}

}
