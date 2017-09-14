package base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/14.
 */
public class EntityStorage<T extends IEntity> {

	protected Map<Long, T> entityMap;

	public EntityStorage() {
		entityMap = new HashMap<Long, T>();
	}

	public Map<Long, T> getEntityMap() {
		return entityMap;
	}

	public void addEntity(T t) {
		entityMap.put(t.getEntityId(), t);
	}

	public void remove(T t) {
		entityMap.remove(t.getEntityId());
	}

	public void remove(long id) {
		entityMap.remove(id);
	}

	public T getEntity(long id) {
		return entityMap.get(id);
	}

	public Collection<T> getAllEntities() {
		return entityMap.values();
	}

	public Collection<T> getAllEntitiesSafe() {
		synchronized (this) {
			return new ArrayList<>(entityMap.values());
		}
	}
}
