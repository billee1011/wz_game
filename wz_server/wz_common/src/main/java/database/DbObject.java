package database;

import java.util.HashMap;

/**
 * Created by WZ on 2016/9/2 0002.
 */
public class DbObject extends HashMap{

	public int getInt(String key){
		return get(key) == null ? 0 : (Integer)get(key);
	}

	public Long getLong(String key){
		return get(key) == null ? 0 : (Long)get(key);
	}

	public Boolean getBool(String key){
		return get(key) == null ? false : (Boolean)get(key);
	}

	public String getString(String key){
		return get(key) == null ? "" : (String)get(key);
	}

	public short getShort(String key){
		return get(key) == null ? 0 : (short)get(key);
	}

	public byte getByte(String key){
		return get(key) == null ? 0 : (byte)get(key);
	}

	public float getFloat(String key){
		return get(key) == null ? 0 : (float)get(key);
	}

	public double getDouble(String key){
		return get(key) == null ? 0 : (double)get(key);
	}
}
