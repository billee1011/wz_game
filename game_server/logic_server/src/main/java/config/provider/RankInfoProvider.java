package config.provider;

import config.JsonUtil;
import config.bean.Rank;
import database.DataQueryResult;
import util.ASObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/9.
 */
public class RankInfoProvider extends BaseProvider {
	private static RankInfoProvider inst = new RankInfoProvider();

	private RankInfoProvider() {

	}

	public static RankInfoProvider getInst() {
		return inst;
	}

	static {
		BaseProvider.providerList.add(inst);
	}

	private Map<Integer, Rank> rankMap = null;

	@Override
	public void doLoad() {
//		rankMap = JsonUtil.getJsonMap(Rank[].class, "rank.json");
		getRankRewardInfo();
	}


	private void getRankRewardInfo() {
		Map<Integer, Rank> rankMap = new HashMap<>();
		List<ASObject> rankList = DataQueryResult.load("select * from ranking");
		for (ASObject rankInfo : rankList) {
			int id = rankInfo.getInt("id");
			Rank rank = new Rank();
			rank.setId(id);
			rank.setGold(rankInfo.getInt("gold"));
			int rankBegin = rankInfo.getInt("rankBegin");
			rank.setFromRank(rankBegin);
			int rankEnd = rankInfo.getInt("rankEnd");
			rank.setToRank(rankEnd);

			if (0 == rankEnd || rankBegin == rankEnd) {
				rank.setRank(Integer.toString(rankBegin));
			} else {
				rank.setRank(Integer.toString(rankBegin) + "," + Integer.toString(rankEnd));
			}

			rankMap.put(id, rank);
		}
		this.rankMap = rankMap;
	}

	public Map<Integer, Rank> getRankMap() {
		return rankMap;
	}

	public int getRankReward(int rank) {
		for (Rank conf : rankMap.values()) {
			if (conf.getToRank() == 0) {
				if (rank == conf.getFromRank()) {
					return conf.getGold();
				}
			} else {
				if (rank >= conf.getFromRank() && rank <= conf.getToRank()) {
					return conf.getGold();
				}
			}
		}
		return 0;
	}

	@Override
	protected void initString() {
		confString = JsonUtil.getGson().toJson(rankMap, Map.class);
	}
}
