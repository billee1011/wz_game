package util;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

public class XProperties extends Properties {

	private static final long serialVersionUID = -7234946412423331719L;

	private static final Charset PROPERTIES_ENCODING = Charset.forName("UTF-8");

	private static final String DEFAULT_CFG_DB = "cfg";
	private static final String DEFAULT_CFG_TABLE = "config";

	public XProperties() {
	}

	public XProperties(Properties defaults) {
		super(defaults);
	}


	public void load(String path) {
		try {
			load(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load(File file) throws IOException {
		InputStreamReader is = null;
		try {
			is = new InputStreamReader(new FileInputStream(file), PROPERTIES_ENCODING);
			this.load(is);
		} finally {
			try {
				is.close();
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public void loadResource(String name) throws IOException {
		URL configUrl = ClassLoader.getSystemResource(name);
		if (configUrl == null) {
			throw new IOException("system resource not found: " + name);
		}
		try {
			this.load(new File(configUrl.toURI()));
		} catch (Exception e) {
			throw new IOException("invalid system resource: " + name, e);
		}
	}

	public void loadFile(String filepath) throws IOException {
		this.load(new File(filepath));
	}


	public void setByte(String key, byte value) {
		this.setProperty(key, Byte.toString(value));
	}

	public Byte getByte(String key, Byte def) {
		try {
			String val = getProperty(key);
			if (val == null) {
				return def;
			}
			return Byte.parseByte(val);
		} catch (Exception e) {
			return def;
		}
	}

	public void setShort(String key, short value) {
		this.setProperty(key, Short.toString(value));
	}

	public Short getShort(String key, Short def) {
		try {
			return Short.parseShort(getProperty(key));
		} catch (Exception e) {
			return def;
		}
	}

	public void setInteger(String key, int value) {
		this.setProperty(key, Integer.toString(value));
	}

	public Integer getInteger(String key, Integer def) {
		try {
			String val = getProperty(key);
			if (val == null) {
				return def;
			}
			return Integer.parseInt(val);
		} catch (Exception e) {
			return def;
		}
	}

	public void setLong(String key, long value) {
		this.setProperty(key, Long.toString(value));
	}

	public Long getLong(String key, Long def) {
		try {
			String val = getProperty(key);
			if (val == null) {
				return def;
			}
			return Long.parseLong(val);
		} catch (Exception e) {
			return def;
		}
	}

	public void setBoolean(String key, boolean value) {
		this.setProperty(key, Boolean.toString(value));
	}

	public Boolean getBoolean(String key, Boolean def) {
		Integer value = getInteger(key, null);
		if (value != null) {
			return value != 0;
		}
		try {
			return Boolean.parseBoolean(getProperty(key));
		} catch (Exception e) {
			return def;
		}
	}

	public void setString(String key, String value) {
		if (value == null) {
			value = "";
		}
		this.setProperty(key, value);
	}

	public String getString(String key) {
		return getString(key, "");
	}

	public String getString(String key, String def) {
		String value = getProperty(key, def);
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

	@Override
	public String getProperty(String key) {
		String ret = super.getProperty(key);
		return ret == null ? null : ret.trim();
	}
}
