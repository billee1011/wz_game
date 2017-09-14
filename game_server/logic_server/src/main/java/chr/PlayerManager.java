package chr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by think on 2017/9/8.
 */
public class PlayerManager {

	private static PlayerManager instance = new PlayerManager();

	private PlayerManager() {

	}

	public static PlayerManager getInst() {
		return instance;
	}

	private Map<Long, RyCharacter> characterMap = new ConcurrentHashMap<>();

	private Map<String, RyCharacter> name2PlayerMap = new ConcurrentHashMap<>();


	public void addPlayer(RyCharacter ch) {
		characterMap.put(ch.getEntityId(), ch);
		name2PlayerMap.put(ch.getPlayerName(), ch);
	}

	public Collection<RyCharacter> getAllPlayers() {
		synchronized (this) {
			return new ArrayList<>(characterMap.values());
		}
	}

	public RyCharacter getCharacter(long entityId) {
		return characterMap.get(entityId);
	}

	public void saveAllCharacter(long intervalTime) {

	}

	public void removeCharacter(RyCharacter ch) {

	}
}
