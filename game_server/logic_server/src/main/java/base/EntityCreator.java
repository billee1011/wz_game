package base;

import chr.RyCharacter;
import chr.hero.HeroEntity;
import define.EntityType;
import service.LogicApp;
import util.MiscUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by think on 2017/9/8.
 */
public class EntityCreator {
    private static Map<EntityType, AtomicLong> entityCountMap = new HashMap<>();

    static {
        for (EntityType entityType : EntityType.values()) {
            entityCountMap.put(entityType, new AtomicLong(0));
        }
    }

    public static RyCharacter createChar(String name, int userId) {
        return RyCharacter.getDefault(geneEntityId(EntityType.CHARACTER), name, userId);
    }


    public static HeroEntity createHero(int confId) {
        return HeroEntity.createHeroEntity(geneEntityId(EntityType.HERO), confId);
    }

    private static long geneEntityId(EntityType entityType) {
        long currentSecond = MiscUtil.getCurrentSeconds();
        long typeValue = entityType.getValue();
        long serverId = LogicApp.getInst().getServerId();
        long itemCount = entityCountMap.get(entityType).getAndIncrement() % 0xffff;
        long entityId = 0;
        entityId = (entityId & ENTITY_TYPE_MASK) + (typeValue << 56);
        entityId = (entityId & CREATE_TIME_MASK) + (currentSecond << 24);
        entityId = (entityId & SERVER_ID_MASK) + (serverId << 12);
        entityId = (entityId & ENTITY_COUNT_MASK) + itemCount;
        return entityId;
    }

    private static final long ENTITY_TYPE_MASK = 0x00FFFFFFFFFFFFFFL;

    private static final long CREATE_TIME_MASK = 0xFF00000000FFFFFFL;

    private static final long SERVER_ID_MASK = 0xFFFFFFFFFF000FFFL;

    private static final long ENTITY_COUNT_MASK = 0xFFFFFFFFFFFFF000L;

}
