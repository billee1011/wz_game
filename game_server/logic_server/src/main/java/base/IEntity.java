package base;

import define.EntityType;

/**
 * Created by think on 2017/9/7.
 */
public class IEntity {
	private EntityType entityType;


	private long entityId;

	public IEntity() {

	}


	public IEntity(EntityType entityType, long entityId) {
		this.entityType = entityType;
		this.entityId = entityId;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}
}
