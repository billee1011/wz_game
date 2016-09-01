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

	public static List<Map<String, Object>> executeQuery(String tableName, Map<String, Object> where) {
		return executeQuery(tableName, where, null);
	}

	public static List<Map<String, Object>> executeQuery(String tableName, Map<String, Object> where, List<String> searchFields) {
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

	public static List<Map<String, Object>> fillResult(ResultSet set) throws SQLException {
		List<Map<String, Object>> result = new ArrayList<>();
		while (set.next()) {
			result.add(fillOneResult(set));
		}
		return result;
	}

	public static Map<String, Object> fillOneResult(ResultSet set) throws SQLException {
		Map<String, Object> result = new HashMap<>();
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

	public static String getSql(String tableName, Map<String, Object> params, List<String> fieldsList) {
		if (fieldsList == null) {
			return getSqlString(tableName, params);
		} else {
			return getFieldSqlString(tableName, params, fieldsList);
		}
	}

	private static String getFieldSqlString(String tableName, Map<String, Object> params, List<String> fieldsList) {
		return "";
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
