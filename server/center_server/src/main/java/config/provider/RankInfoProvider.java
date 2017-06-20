package config.provider;

import config.JsonUtil;
import config.bean.Rank;

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
	public void loadConfig() {
		rankMap = JsonUtil.getJsonMap(Rank[].class, "rank.json");
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
}
