package chr;

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

    }

    public void removeCharacter(RyCharacter ch) {

    }
}
