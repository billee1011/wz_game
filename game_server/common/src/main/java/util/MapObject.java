package util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapObject extends HashMap<String, Object> {
    /**
     * 
     */
    private static final long serialVersionUID = -3547594935968099444L;
    private String typeName = "";

    public MapObject() {
        this("");
    }

    public MapObject(String typeName) {
        super(8);
        if (typeName == null) {
            typeName = "";
        }
        this.typeName = typeName.trim();
    }

    public MapObject(Object... keyvalueList) {
        this("");
        for (int i = 0; i + 1 < keyvalueList.length; i += 2) {
            this.put((String) keyvalueList[i], keyvalueList[i + 1]);
        }
    }

    
    public MapObject(Map<String, Object> map) {
        super(map);
    }

    public String getTypeName() {
        return this.typeName;
    }

    public boolean isTypedObject() {
        return this.typeName != null && !this.typeName.isEmpty();
    }

    /**
     * 
     */
    @Override
    public MapObject clone() {
        return new MapObject(this);
    }

    private Number getNumber(String key, Number def) {
        Object value = get(key);
        if (value == null) {
            return def;
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        return def;
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int def) {
        Number numValue = getNumber(key, null);
        if (numValue == null) {
            try {
                String strValue = getString(key);
                if (strValue != null) {
                    numValue = Integer.parseInt(strValue);
                }
                else {
                    numValue = def;
                }
            }
            catch (Exception e) {
                numValue = def;
            }
        }
        return numValue.intValue();
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(String key, long def) {
        Number numValue = getNumber(key, null);
        if (numValue == null) {
            try {
                String strValue = getString(key);
                if (strValue != null) {
                    numValue = Long.parseLong(strValue.trim());
                }
                else {
                    numValue = def;
                }
            }
            catch (Exception e) {
                numValue = def;
            }
        }
        return numValue.longValue();
    }
    
    public String getString(String key) {
        return (String) get(key);
    }

    public Date getTimestamp(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        else if (value instanceof Date) {
            return (Date) value;
        }
        else if (value instanceof Number || value instanceof String) {
            long ts = getLong(key);
            if (ts == 0) {
                return null;
            }
            return new Date(ts);
        }
        else {
            return null;
        }
    }

    public boolean getBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value).booleanValue();
        }
        else if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        else if (value instanceof String) {
            return ((String) value).length() > 0;
        }
        else {
            return true;
        }
    }

    public MapObject getASObject(String key) {
        return (MapObject) get(key);
    }
}
