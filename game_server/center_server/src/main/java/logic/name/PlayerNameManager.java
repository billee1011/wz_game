package logic.name;

import chr.PlayerManager;
import database.DataQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ASObject;
import util.Randomizer;

import java.util.*;

/**
 * Created by Administrator on 2017/3/10.
 */
public class PlayerNameManager {
	private static final Logger logger = LoggerFactory.getLogger(PlayerNameManager.class);
	private static PlayerNameManager instance = new PlayerNameManager();

	private PlayerNameManager() {

	}

	public static PlayerNameManager getInst() {
		return instance;
	}

	private Object nameLock = new Object();

	private List<String> randomOne = new ArrayList<>();
	private List<String> randomTwo = new ArrayList<>();

	private Map<String, Boolean> useNameMap = new HashMap<>();

	public void loadNameInfo() {
		long time1 = System.currentTimeMillis();
		randomOne.clear();
		useNameMap.clear();
		randomTwo.clear();
		List<ASObject> randomNameList = DataQueryResult.load("select * from random_name");
		randomNameList.forEach(e -> {
			String first = e.getString("first");
			if (!first.equals("")) {
				randomOne.add(first);
			}
			String last = e.getString("last");
			if (!last.equals("")) {
				randomTwo.add(last);
			}
		});
		List<ASObject> usedNameList = DataQueryResult.load("select nickname from player");
		usedNameList.forEach(e -> useNameMap.put(e.getString("nick_name"), true));
		long time2 = System.currentTimeMillis();
		logger.info(" the load name cost time is {}", (time2 - time1));
	}


	public String randomName4NewPlayer() {
		int size1 = randomOne.size();
		int size2 = randomTwo.size();
		for (int i = 0; i < 100000; i++) {
			int random1 = Randomizer.nextInt(size1);
			int random2 = Randomizer.nextInt(size2);
			String name = randomOne.get(random1) + randomTwo.get(random2);
			if (isNickNameAvailable(name, null)) {
				return name;
			}
		}
		return "";
	}

	public boolean isNickNameAvailable(String name, String removeName) {
		synchronized (nameLock) {
			if (useNameMap.containsKey(name)) {
				return false;
			}
			useNameMap.put(name, true);
			if (removeName != null){
				useNameMap.remove(removeName);
			}
			return true;
		}
	}

	public Collection<String> random20NameForClient() {
		Set<String> result = new HashSet<>();
		int size1 = randomOne.size();
		int size2 = randomTwo.size();
		while (true) {
			int random1 = Randomizer.nextInt(size1);
			int random2 = Randomizer.nextInt(size2);
			result.add(randomOne.get(random1) + randomTwo.get(random2));
			if (result.size() >= 20) {
				break;
			}
		}
		return result;
	}

}
