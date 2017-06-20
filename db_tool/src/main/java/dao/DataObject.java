package dao;

import java.util.HashMap;

/**
 * Created by think on 2017/3/27.
 */
public class DataObject extends HashMap<String, Object> {

    public int getInt(String key, int def) {
        Object value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof Integer) {
            return (int) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (Exception e) {
                return def;
            }
        }
        return def;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public String getString(String key, String def) {
        Object value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return "";
    }

    public String getString(String key) {
        return getString(key, "");
    }

    public boolean getBoolean(String key, Boolean def) {
        Object value = get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    public Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
}
