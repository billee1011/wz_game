package util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by WZ on 2016/8/25.
 */
public class WzProperties {
	private Properties props = new Properties();

	public WzProperties() {

	}

	public WzProperties(InputStream stream) {
		try {
			props.load(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WzProperties(Properties props) {
		this.props = props;
	}

	public WzProperties(String filePath) {
		try {
			InputStream in = new FileInputStream(filePath);
			props.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getInt(String key, int defValue) {
		try {
			return Integer.parseInt(props.getProperty(key));
		} catch (Exception e) {
			return defValue;
		}
	}

	public short getShort(String key) {
		return getShort(key, (short)0);
	}

	public short getShort(String key, short defValue) {
		try {
			return Short.parseShort(props.getProperty(key));
		} catch (Exception e) {
			return defValue;
		}
	}


	public String getString(String key, String defValue) {
		return props.getProperty(key, defValue);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return false;
	}

}
