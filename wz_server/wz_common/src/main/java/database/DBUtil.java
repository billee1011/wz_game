package database;

import javax.xml.transform.sax.SAXTransformerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by WZ on 2016/9/1 0001.
 */
public class DBUtil {

	public static List<DbObject> executeQuery(String tableName, DbObject where) {
		return executeQuery(tableName, where, null);
	}

	public static List<DbObject> executeQuery(String tableName, DbObject where, List<String> searchFields) {
		Connection conn = DBManager.getInst().getConnection();
		List<Map<String, Object>> result = null;
		try {
			PreparedStatement stat = conn.prepareStatement(getSql(tableName, where, searchFields));
			Set<String> keySet = where.keySet();
			int index = 0;
			for (String s : keySet) {
				index++;
				setPreparedParams(index, stat, where.get(s));
			}
			return fillResult(stat.executeQuery());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean executeInsert(String tableName, DbObject data) {
		Connection conn = DBManager.getInst().getConnection();
		try {
			PreparedStatement stat = conn.prepareStatement(getSqlInertString(tableName, data));
			Set<String> keys = data.keySet();
			int index = 0;
			for (String key : keys) {
				index++;
				setPreparedParams(index, stat, data.get(key));
			}
			return stat.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static List<DbObject> fillResult(ResultSet set) throws SQLException {
		List<DbObject> result = new ArrayList<>();
		while (set.next()) {
			result.add(fillOneResult(set));
		}
		return result;
	}

	private static DbObject fillOneResult(ResultSet set) throws SQLException {
		DbObject result = new DbObject();
		ResultSetMetaData data = set.getMetaData();
		int count = data.getColumnCount();
		for (int i = 0; i < count; i++) {
			String name = data.getColumnName(i + 1);
			result.put(name, set.getObject(name));
		}
		return result;
	}

	private static void setPreparedParams(int index, PreparedStatement stat, Object obj) throws SQLException {
		if (obj instanceof Integer) {
			stat.setInt(index, (Integer) obj);
		} else if (obj instanceof Boolean) {
			stat.setBoolean(index, (Boolean) obj);
		} else if (obj instanceof String) {
			stat.setString(index, (String) obj);
		} else {
			//do nothing
		}
	}

	private static String getSql(String tableName, Map<String, Object> params, List<String> fieldsList) {
		if (fieldsList == null) {
			return getSqlString(tableName, params);
		} else {
			return getFieldSqlString(tableName, params, fieldsList);
		}
	}

	private static String getFieldSqlString(String tableName, Map<String, Object> params, List<String> fieldsList) {
		return "";
	}

	private static String getSqlInertString(String tableName, DbObject data) {
		StringBuilder sql = new StringBuilder();
		StringBuilder values = new StringBuilder();
		sql.append("INSERT INTO ").append(tableName);
		values.append(" VALUES ");
		String[] keys = (String[]) data.keySet().toArray(new String[0]);
		Object[] params = new Object[keys.length];
		for (int i = 0; i < keys.length; ++i) {
			String key = keys[i];
			params[i] = data.get(key);
			String delim = (i == 0 ? "(" : ",");
			sql.append(delim);
			values.append(delim);
			sql.append('`').append(key).append('`');
			values.append("?");
		}
		sql.append(")");
		values.append(")");
		sql.append(values.toString());
		return sql.toString();
	}

	private static String getSqlString(String tableName, Map<String, Object> params) {
		StringBuffer buffer = new StringBuffer(50);
		buffer.append("select * from ");
		buffer.append(tableName);
		if (params.size() == 0) {
			buffer.append(";");
		} else {
			buffer.append(" where ");
			int index = 0;
			for (String key : params.keySet()) {
				buffer.append(key);
				buffer.append((" = ?"));
				index++;
				if (index != params.size()) {
					buffer.append(" and ");
				}
			}
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
	}
}
