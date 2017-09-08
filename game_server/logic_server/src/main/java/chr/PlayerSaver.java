package chr;

import chr.hero.CharHero;
import chr.hero.HeroEntity;
import config.JsonUtil;
import database.DBUtil;
import define.EMoney;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/8.
 */
public class PlayerSaver {
    private static Logger logger = LoggerFactory.getLogger(PlayerSaver.class);

    public static void insertPlayer(RyCharacter ch) throws SQLException {
        Map<String, Object> data = new HashMap<>();
        data.put("player_id", ch.getEntityId());
        data.put("diamond", ch.getResourceManager().getResCount(EMoney.DIAMOND));
        data.put("silver", ch.getResourceManager().getResCount(EMoney.SILVER));
        data.put("reputation", ch.getResourceManager().getResCount(EMoney.REPUTATION));
        data.put("player_name", ch.getPlayerName());
        data.put("user_id", ch.getUserId());
        DBUtil.executeInsert("player", data);
        for (HeroEntity heroEntity : ch.getCharHero().getHeroMap().values()) {
            Map<String, Object> heroData = new HashMap<>();
            heroData.put("player_id", ch.getEntityId());
            heroData.put("hero_id", heroEntity.getEntityId());
            heroData.put("conf_id", heroEntity.getHeroId());
            heroData.put("level", heroEntity.getLevel());
            heroData.put("exp", heroEntity.getExp());
            heroData.put("break_level", heroEntity.getBreakLevel());
            heroData.put("awake_level", heroEntity.getAwakeLevel());
            heroData.put("awake_info", JsonUtil.getGson().toJson(heroEntity.getAwakeInfo(), int[].class));
            heroData.put("tianming_level", heroEntity.getTianmingLevel());
            DBUtil.executeInsert("char_hero", heroData);
        }
    }
}
