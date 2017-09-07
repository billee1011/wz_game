package rank;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;

/**
 * Created by Administrator on 2016/11/28.
 */
public class RankManager {
	private static Logger logger = LoggerFactory.getLogger(RankManager.class);
	private static RankManager instance = new RankManager();

	private RankManager() {

	}

	public static RankManager getInst() {
		return instance;
	}

	private static final int COUNT = 100;


	private List<RankItem> rankItems = null;

	public void getRankItemList(Player player) {

	}


	public void freshRankList(boolean sendReward) {

	}

}