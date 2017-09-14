package chr;

import base.EntityStorage;
import db.DataManager;
import service.LogicApp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by think on 2017/9/8.
 */
public class PlayerManager extends EntityStorage<RyCharacter> {

	private static PlayerManager instance = new PlayerManager();

	private PlayerManager() {

	}

	public static PlayerManager getInst() {
		return instance;
	}


	public void saveAllCharacter() {
		getAllEntitiesSafe().forEach(this::saveRyCharacter);
	}

	private void saveRyCharacter(RyCharacter ch) {
		long time = System.currentTimeMillis();
		if (time - ch.getLastSaveTime() >= LogicApp.SAVE_INTERNAL) {
			PlayerSaver.savePlayer(ch);
		}
	}

	public void removeCharacter(RyCharacter ch) {

	}
}
