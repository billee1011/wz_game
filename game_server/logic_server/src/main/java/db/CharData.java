
package db;

import db.data.DBAction;
import db.data.IModuleData;
import db.data.module.CommonModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MapObject;
import util.MiscUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

public class CharData {
    private static final Logger logger = LoggerFactory.getLogger(CharData.class);

    private long charId = 0;
    private final Map<DBAction, IModuleData> modules = MiscUtil.newArrayMap();

    public CharData() {
        // donothing
    }

    public void init(long charId) {
        this.charId = charId;
        modules.put(DBAction.PLAYER, new CommonModule(this.charId, "player", "player_id", Arrays.asList("player_id"), false));
        modules.put(DBAction.HERO, new CommonModule(this.charId, "char_hero", "player_id", Arrays.asList("player_id", "hero_id"), false));
        modules.put(DBAction.EQUIP, new CommonModule(this.charId, "char_equip", "player_id", Arrays.asList("player_id", "equip_id"), false));
        modules.put(DBAction.FORMATION, new CommonModule(this.charId, "char_formation", "player_id", Arrays.asList("player_id"), false));
    }

    public long getCharId() {
        return this.charId;
    }

    public boolean loadAll() throws SQLException {
        for (Entry<DBAction, IModuleData> modEntry : modules.entrySet()) {
            IModuleData mod = modEntry.getValue();
            mod.load();
        }
        logger.debug(" load player {} success ", charId);
        return true;
    }

    public void saveAll() throws SQLException {
        for (Entry<DBAction, IModuleData> modEntry : modules.entrySet()) {
            IModuleData mod = modEntry.getValue();
            try {
                synchronized (mod) {
                    mod.save();
                }
            } catch (Exception e) {
                logger.error("", e);
            }
        }
    }


    public MapObject getModuleData(DBAction action) {
        IModuleData mod = modules.get(action);
        if (mod == null) {
            return null;
        }
        synchronized (mod) {
            return mod.getData();
        }
    }

    public void updateModuleData(DBAction action, MapObject moduleData) {
        IModuleData mod = modules.get(action);
        if (mod == null) {
            return;
        }
        if (moduleData == null) {
            return;
        }
        synchronized (mod) {
            mod.update(moduleData);
        }
    }

    public DBAction checkSame(CharData newData) {
        for (Entry<DBAction, IModuleData> modEntry : modules.entrySet()) {
            IModuleData mod = modEntry.getValue();
            boolean ret = mod.checkSame(newData.getModuleData(modEntry.getKey()));
            if (false == ret) {
                return modEntry.getKey();
            }
        }
        return null;
    }
}
