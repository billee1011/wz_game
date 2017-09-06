package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MsgProvider {

    private static final Logger logger = LoggerFactory.getLogger(MsgProvider.class);

    private static final MsgProvider inst = new MsgProvider();

    public static final MsgProvider getInst() {
        return inst;
    }

    private final XProperties langProperties = new XProperties();
    private final Object kMutex = new Object();

    public boolean load() {
        XProperties cfg = new XProperties();
        try {
            cfg.loadFile("config/config.properties");
        }
        catch (Exception e) {
            return false;
        }
        String lang = cfg.getString("coco.lang", "zh_CN");
        return load(lang);
    }

    public boolean load(String lang) {
        XProperties tmp = new XProperties();
        // 1. try lang/lang.properties
        try {
            tmp.loadFile("lang/lang.properties");
        }
        catch (Exception e1) {
            logger.info("Load lang/lang.properties error, try lang_${locale}.properties");
            // 2. try lang/lang_${locale}.properties
            tmp = new XProperties();
            try {
                tmp.loadFile("lang/lang_" + lang + ".properties");
            }
            catch (Exception e2) {
                logger.error("", e2);
                return false;
            }
        }
        synchronized (kMutex) {
            langProperties.clear();
            for (Object keyObj : tmp.keySet()) {
                String key = (String) keyObj;
                langProperties.put(key.toLowerCase(), tmp.get(key));
            }
        }
        return true;
    }

    public static String getString(String path, String def, String... params) {
        return getInst().getValue(path, def, MiscUtil.newParamsMap(params));
    }

    public static String getString(String path, String def) {
        return getInst().getValue(path, def);
    }

    public static String getString(String path, String def, Map<String, String> params) {
        return getInst().getValue(path, def, params);
    }

    public String getValue(String path, String def) {
        return getValue(path, def, null);
    }

    public String getValue(String path, String def, Map<String, String> params) {
        if (langProperties == null) {
            return MiscUtil.replaceTemplate(def, params);
        }
        if (path == null) {
            return MiscUtil.replaceTemplate(def, params);
        }
        if ((path = path.trim()).isEmpty()) {
            return MiscUtil.replaceTemplate(def, params);
        }
        String value = def;
        String key = path.toLowerCase();
        synchronized (kMutex) {
            value = langProperties.getString(key, def);
        }
        return MiscUtil.replaceTemplate(value, params);
    }

}
