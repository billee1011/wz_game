package chr.resource;

import chr.RyCharacter;
import define.EMoney;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/7.
 */
public class ResourceManager {
    private static Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    private RyCharacter character;

    private Map<EMoney, Long> resourceMap;


    public ResourceManager(RyCharacter character) {
        this.character = character;
        this.resourceMap = new HashMap<>();
        for (EMoney eMoney : EMoney.values()) {
            resourceMap.put(eMoney, 0L);
        }
    }

    public void setResourceMap(Map<EMoney, Long> resMap) {
        this.resourceMap = resMap;
    }

    public Map<EMoney, Long> getResourceMap() {
        return resourceMap;
    }

    public void updateResource(EMoney type, long value, boolean add) {
        if (add)
            resourceMap.merge(type, value, (e, f) -> e == null ? 0 + f : e + f);
        else
            resourceMap.merge(type, value, (e, f) -> e == null ? 0 : e - f > 0 ? e - f : 0);
    }


    public long getResCount(EMoney type) {
        return resourceMap.get(type);
    }

    public void updateResource(int id, long value, boolean add) {
        EMoney money = EMoney.getByValue(id);
        if (money == null) {
            logger.warn("update resource that don't exist {}", id);
        }
        updateResource(money, value, add);
    }

}
