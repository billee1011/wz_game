package database;

import log.ILog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.MiscUtil;
import util.StringUtil;

import java.sql.*;
import java.util.*;


public class DBUtil {

	private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

	public static ILog fileLogger = null;
	public static boolean kDebugginSQL = false;

	public static void execute(String sql) {
		execute(sql, new Object[0]);
	}

	public static void execute(String sql, Object[] params) {
		execute(sql, params, null);
	}

	public static void execute(String db, String sql, Object[] params) {
		execute(db, sql, params, null);
	}

	public static void execute(String sql, Object[] params, IQueryHandler handler) {
		execute(DBManager.getDefaultDatabase(), sql, params, handler);
	}


	public static void execute(String sql, Map<String, Object> params) {
		execute(sql, params, null);
	}

	public static void execute(String db, String sql, Map<String, Object> params) {
		execute(db, sql, params, null);
	}

	public static void execute(String sql, Map<String, Object> params, IQueryHandler handler) {
		execute(DBManager.getDefaultDatabase(), sql, params, handler);
	}

	private static class KeyNode {
		private String key;
		private int position;

		public KeyNode(String key, int position) {
			this.key = key;
			this.position = position;
		}

		public String getKey() {
			return key;
		}

		public int getPosition() {
			return position;
		}
	}

	private static void findAllNodes(String sql, String key, List<KeyNode> nodeList) {
		if (key == null || (key = key.trim()).isEmpty()) {
			return;
		}
		int index = 0;
		while (true) {
			index = sql.indexOf(key, index);
			if (index < 0) {
				break;
			}
			KeyNode keyNode = new KeyNode(key, index);
			nodeList.add(keyNode);
			index += key.length();
		}
	}

	public static void execute(String db, String sql, Map<String, Object> params, IQueryHandler handler) {
		List<KeyNode> nodeList = MiscUtil.newArrayList();
		for (String key : params.keySet()) {
			findAllNodes(sql, key, nodeList);
		}
		Collections.sort(nodeList, new Comparator<KeyNode>() {

			@Override
			public int compare(KeyNode o1, KeyNode o2) {
				return o1.getPosition() - o2.getPosition();
			}
		});
		List<Object> paramsArray = MiscUtil.newArrayList();
		for (KeyNode node : nodeList) {
			sql = sql.replace(node.getKey(), "?");
			Object value = params.get(node.getKey());
			paramsArray.add(value);
		}
		// exe
		execute(db, sql, paramsArray.toArray(), handler);
	}

	public static PreparedStatement execute(Connection conn, String sql, Object[] params) throws SQLException {
		return execute(conn, DBManager.getDefaultDatabase(), sql, params);
	}

	public static PreparedStatement execute(Connection conn, String db, String sql, Object[] params)
			throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		if (params != null) {
			for (int i = 0; i < params.length; ++i) {
				if (params[i] instanceof DBValue) {
					DBValue o = (DBValue) params[i];
					ps.setObject(i + 1, o.getValue(), o.getType());
				}
//                else if (params[i] instanceof ByteArray) {
//                    ByteArray buffer = (ByteArray) params[i];
//                    ps.setBlob(i + 1, new ByteArrayInputStream(buffer.toArray()));
//                }
				else {
					ps.setObject(i + 1, params[i]);
				}
			}
		}

		// / 测试
		if (kDebugginSQL) {
			StringBuilder sb = new StringBuilder();
			sb.append("SQL = ").append(sql).append(";  ");
			if (params != null) {
				sb.append("PARAMS = ");
				for (int i = 0; i < params.length; ++i) {
					sb.append(params[i]).append(",");
				}
			}
			if (fileLogger != null) {
				fileLogger.log("DBUTIL", sb.toString());
			}
			logger.info("" + sb.toString());
		}

		ps.execute();
		return ps;
	}

	public static void execute(String db, String sql, Object[] params, IQueryHandler handler) {
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DBManager.getConnection(db);
			ps = execute(conn, db, sql, params);
			if (handler != null) {
				handler.onResult(ps);
			}
		} catch (SQLException e) {
			logger.debug("SQL=" + sql, e);
			if (handler != null) {
				handler.onError(e);
			}
		} finally {
			DBManager.close(conn, ps);
		}
	}

	public static long executeInsert(String table, Map<String, Object> data) throws SQLException {
		return executeInsert(DBManager.getDefaultDatabase(), table, data);
	}

	public static int executeDelete(String table, Map<String, Object> where) throws SQLException {
		return executeDelete(DBManager.getDefaultDatabase(), table, where);
	}

	public static int executeDelete(String db, String table, Map<String, Object> where) throws SQLException {
		StringBuilder builder = new StringBuilder();
		List<Object> params = MiscUtil.newArrayList();
		builder.append("DELETE FROM `" + table + "` ");
		getWhereClause(where, builder, params);
		DataQueryHandler handler = new DataQueryHandler() {

			@Override
			public void onResult(PreparedStatement ps) throws SQLException {
				int cnt = ps.getUpdateCount();
				setData(cnt);
			}
		};
		execute(db, builder.toString(), params.toArray(), handler);
		Object result = handler.getData();
		if (result instanceof SQLException) {
			throw (SQLException) result;
		}
		return (Integer) result;
	}

	public static int executeUpdate(String table, Map<String, Object> where, Map<String, Object> data)
			throws SQLException {
		return executeUpdate(DBManager.getDefaultDatabase(), table, where, data);
	}

	public static int executeUpdate(String db, String table, Map<String, Object> where,
									Map<String, Object> data) throws SQLException {
		if (data == null) {
			throw new IllegalArgumentException("where == null || data = null");
		}
		StringBuilder builder = new StringBuilder();
		List<Object> params = MiscUtil.newArrayList();
		builder.append("UPDATE `" + table + "` ");
		getUpdateClause(data, builder, params);
		getWhereClause(where, builder, params);
		DataQueryHandler handler = new DataQueryHandler() {

			@Override
			public void onResult(PreparedStatement ps) throws SQLException {
				int cnt = ps.getUpdateCount();
				setData(cnt);
			}
		};
		execute(db, builder.toString(), params.toArray(), handler);
		Object result = handler.getData();
		if (result instanceof SQLException) {
			throw (SQLException) result;
		}
		return (Integer) result;
	}

	public static void executeQuery(String table, Map<String, Object> where, IQueryHandler handler) {
		executeQuery(table, null, where, handler);
	}

	public static void executeQuery(String table, String[] fields, Map<String, Object> where,
									IQueryHandler handler) {
		executeQuery(DBManager.getDefaultDatabase(), table, fields, where, handler);
	}

	public static ResultSet executeQuery(String sql) {
		String db = DBManager.getDefaultDatabase();
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = DBManager.getConnection(db);
			stat = execute(conn, DBManager.getDefaultDatabase(), sql, new Object[0]);
			return stat.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			DBManager.close(conn);
		}
	}

	public static void executeQuery(String db, String table, String[] fields, Map<String, Object> where,
									IQueryHandler handler) {
		StringBuilder builder = new StringBuilder();
		List<Object> params = MiscUtil.newArrayList();
		String fieldStr = "*";
		if (fields != null && fields.length > 0) {
			fieldStr = StringUtil.joinStringFrom(fields, 0, ", ");
		}
		builder.append("SELECT " + fieldStr + " FROM `" + table + "` ");
		getWhereClause(where, builder, params);
		execute(db, builder.toString(), params.toArray(), handler);
	}

	public static long executeInsert(String db, String table, Map<String, Object> data) throws SQLException {
		if (data == null) {
			throw new IllegalArgumentException("data = null");
		}
		StringBuilder sql = new StringBuilder();
		StringBuilder values = new StringBuilder();
		//
		sql.append("INSERT INTO ").append(table);
		values.append(" VALUES ");
		// keys
		String[] keys = data.keySet().toArray(new String[0]);
		Object[] params = new Object[keys.length];
		for (int i = 0; i < keys.length; ++i) {
			String key = keys[i];
			params[i] = data.get(key);
			//
			String delim = (i == 0 ? "(" : ",");
			sql.append(delim);
			values.append(delim);
			//
			sql.append('`').append(key).append('`');
			values.append("?");
		}
		sql.append(")");
		values.append(")");
		// values
		sql.append(values.toString());
		// execute
		DataQueryHandler handler = new DataQueryHandler() {

			@Override
			public void onResult(PreparedStatement ps) throws SQLException {
				ResultSet rs = ps.getGeneratedKeys();
				long id = 0L;
				if (rs.next()) {
					id = rs.getLong(1);
				}
				setData(id);
			}

			@Override
			public void onError(SQLException e) {
				setData(e);
			}
		};
		execute(db, sql.toString(), params, handler);
		Object result = handler.getData();
		if (result instanceof SQLException) {
			throw (SQLException) result;
		}
		return (Long) result;
	}

	public static int executeSum(String table, String field, Map<String, Object> where) throws SQLException {
		return executeSum(DBManager.getDefaultDatabase(), table, field, where);
	}

	public static int executeCount(String table, Map<String, Object> where) throws SQLException {
		return executeCount(DBManager.getDefaultDatabase(), table, where);
	}

	public static void getUpdateClause(Map<String, Object> data, StringBuilder builder, List<Object> params) {
		getUpdateClause(data, builder, params, null);
	}

	public static void getUpdateClause(Map<String, Object> data, StringBuilder builder, List<Object> params,
									   Collection<String> excludes) {
		if (data.size() > 0) {
			boolean first = true;
			for (String key : data.keySet()) {
				if (excludes != null && excludes.contains(key)) {
					continue;
				}
				builder.append(first ? "SET " : ", ");
				builder.append("`" + key + "` = ? ");
				first = false;
				params.add(data.get(key));
			}
		}
	}

	public static void getWhereClause(Map<String, Object> where, StringBuilder builder, List<Object> params,
									  Collection<String> fields) {
		if (where == null || where.size() == 0) {
			return;
		}
		boolean first = true;
		for (String key : where.keySet()) {
			if (fields != null && !fields.contains(key)) {
				continue;
			}
			builder.append(first ? "WHERE " : "AND ");
			first = false;
			// check
			Object value = where.get(key);
			if (value instanceof Collection) {
				Collection list = (Collection) value;
				if (list.size() == 0) {
					// ignore
					continue;
				} else if (list.size() > 1) {
					builder.append("`").append(key).append("` in (");
					boolean firstKey = true;
					for (Object v : list) {
						builder.append(firstKey ? "" : ", ").append("?");
						firstKey = false;
						params.add(v);
					}
					builder.append(") ");
					continue;
				} else { // size = 1
					value = list.iterator().next();
				}
			}
			// single value
			builder.append("`").append(key).append("` = ? ");
			params.add(value);
		}
	}

	public static void getWhereClause(Map<String, Object> where, StringBuilder builder, List<Object> params) {
		getWhereClause(where, builder, params, null);
	}

	public static int executeSum(String db, String table, String field, Map<String, Object> where)
			throws SQLException {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT SUM(`" + field + "`) FROM `" + table + "` ");
		List<Object> params = MiscUtil.newArrayList();
		getWhereClause(where, builder, params);
		DataQueryHandler handler = new IntDataQueryHandler();
		execute(db, builder.toString(), params.toArray(), handler);
		Object result = handler.getData();
		if (result instanceof SQLException) {
			throw (SQLException) result;
		}
		return (Integer) result;
	}

	public static int executeCount(String db, String table, Map<String, Object> where) throws SQLException {
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT COUNT(*) FROM `" + table + "` ");
		List<Object> params = MiscUtil.newArrayList();
		getWhereClause(where, builder, params);
		DataQueryHandler handler = new IntDataQueryHandler();
		execute(db, builder.toString(), params.toArray(), handler);
		Object result = handler.getData();
		if (result instanceof SQLException) {
			throw (SQLException) result;
		}
		return (Integer) result;
	}

	public static void executeInsertOrUpdate(String table, Map<String, Object> where,
											 Map<String, Object> data, boolean generateKey) throws SQLException {
		executeInsertOrUpdate(DBManager.getDefaultDatabase(), table, where, data, generateKey);
	}

	public static void executeInsertOrUpdate(String db, String table, Map<String, Object> where,
											 Map<String, Object> data, boolean generateKey) throws SQLException {
		int count = DBUtil.executeCount(db, table, where);
		// check
		if (count == 0) {
			// insert
			if (!generateKey) {
				data.putAll(where);
			}
			DBUtil.executeInsert(db, table, data);
		} else {
			// update
			DBUtil.executeUpdate(db, table, where, data);
		}
	}

	/**
	 * array of Map<String, Object>
	 *
	 * @throws SQLException
	 */
	public static void batchInsertOrUpdate(String table, List<String> primarykey, Object[] datalist)
			throws SQLException {
		batchInsertOrUpdate(DBManager.getDefaultDatabase(), table, primarykey, datalist, null);
	}

	public static void batchInsertOrUpdate(String table, List<String> primarykey, Object[] datalist,
										   List<String> excludes) throws SQLException {
		batchInsertOrUpdate(DBManager.getDefaultDatabase(), table, primarykey, datalist, excludes);
	}

	public static boolean exists(Connection conn, String db, String table, Map<String, Object> where,
								 Collection<String> fields) throws SQLException {
		//
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT EXISTS(SELECT * FROM `" + table + "` ");
		List<Object> params = MiscUtil.newArrayList();
		getWhereClause(where, builder, params, fields);
		builder.append(" LIMIT 1) AS result");
		// query
		PreparedStatement ps = null;
		try {
			ps = execute(conn, db, builder.toString(), params.toArray());
			ResultSet rs = ps.getResultSet();
			rs.next();
			int result = rs.getInt("result");
			return result != 0;
		} finally {
			DBManager.close(ps);
		}
	}

	public static void insertInto(Connection conn, String db, String table, Map<String, Object> data,
								  List<String> excludes) throws SQLException {

		StringBuilder sql = new StringBuilder();
		StringBuilder values = new StringBuilder();

		List<String> fields = new ArrayList<String>(data.keySet());
		if (excludes != null && excludes.size() > 0) {
			fields.removeAll(excludes);
		}
		if (fields.size() == 0) {
			return;
		}

		sql.append("INSERT INTO `").append(table).append("` ");
		values.append(" VALUES ");

		List<Object> params = MiscUtil.newArrayList();
		for (String key : fields) {
			params.add(data.get(key));
			//
			String delim = (params.size() == 1 ? "(" : ",");
			sql.append(delim);
			values.append(delim);
			//
			sql.append('`').append(key).append('`');
			values.append("?");
			//
		}
		sql.append(")");
		values.append(")");
		// values
		sql.append(values.toString());
		// execute
		PreparedStatement ps = null;
		try {
			ps = execute(conn, db, sql.toString(), params.toArray());
		} finally {
			DBManager.close(ps);
		}
	}

	public static void update(Connection conn, String db, String table, List<String> primarykey,
							  Map<String, Object> data, List<String> excludes) throws SQLException {
		if (data == null) {
			throw new IllegalArgumentException("where == null || data = null");
		}

		StringBuilder builder = new StringBuilder();
		List<Object> params = MiscUtil.newArrayList();
		builder.append("UPDATE `" + table + "` ");
		getUpdateClause(data, builder, params, excludes);
		getWhereClause(data, builder, params, primarykey);

		PreparedStatement ps = null;
		try {
			ps = execute(conn, db, builder.toString(), params.toArray());
		} finally {
			DBManager.close(ps);
		}
	}

	public static void batchInsertOrUpdate(String db, String table, List<String> primarykey,
										   Object[] datalist, List<String> excludes) throws SQLException {
		if (datalist == null) {
			throw new IllegalArgumentException("data = null");
		}
		if (datalist.length == 0) {
			return;
		}
		for (int i = 0; i < datalist.length; ++i) {
			if (!(datalist[i] instanceof Map<?, ?>)) {
				throw new IllegalArgumentException("data components should be Map<String, Object>");
			}
		}

		Connection conn = null;
		try {
			conn = DBManager.getConnection(db);
			conn.setAutoCommit(false);
			for (int idx = 0; idx < datalist.length; ++idx) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) datalist[idx];
				if (exists(conn, db, table, data, primarykey)) {
					update(conn, db, table, primarykey, data, excludes);
				} else {
					insertInto(conn, db, table, data, excludes);
				}
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (Exception ex) {
				// ignore
			}
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception e) {
				// ignore
			}
			DBManager.close(conn);
		}

	}

	public static void batchInsertOrUpdateOld(String db, String table, List<String> primarykey,
											  Object[] datalist, List<String> excludes) throws SQLException {
		if (datalist == null) {
			throw new IllegalArgumentException("data = null");
		}
		if (datalist.length == 0) {
			return;
		}
		for (int i = 0; i < datalist.length; ++i) {
			if (!(datalist[i] instanceof Map<?, ?>)) {
				throw new IllegalArgumentException("data components should be Map<String, Object>");
			}
		}

		Connection conn = null;
		try {
			conn = DBManager.getConnection(db);
			conn.setAutoCommit(false);
			for (int idx = 0; idx < datalist.length; ++idx) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) datalist[idx];

				StringBuilder sql = new StringBuilder();
				StringBuilder values = new StringBuilder();
				StringBuilder dupldate = new StringBuilder();

				String[] keys = data.keySet().toArray(new String[0]);
				sql.append("INSERT INTO `").append(table).append("` ");
				values.append(" VALUES ");
				dupldate.append(" ON DUPLICATE KEY UPDATE ");

				List<Object> params = MiscUtil.newArrayList();
				int dIdx = 0;
				for (int i = 0; i < keys.length; ++i) {
					String key = keys[i];

					if (excludes != null && excludes.contains(key)) {
						continue;
					}

					params.add(data.get(key));
					//
					String delim = (params.size() == 1 ? "(" : ",");
					sql.append(delim);
					values.append(delim);
					//
					sql.append('`').append(key).append('`');
					values.append("?");
					//
					if (!primarykey.contains(key)) {
						if (dIdx > 0) {
							dupldate.append(", ");
						}
						dupldate.append("`").append(key).append("` = VALUES(`");
						dupldate.append(key).append("`)");
						++dIdx;
					}
				}
				sql.append(")");
				values.append(")");
				// values
				sql.append(values.toString());
				sql.append(dupldate.toString());
				// execute
				PreparedStatement ps = null;
				try {
					ps = execute(conn, db, sql.toString(), params.toArray());
				} finally {
					DBManager.close(ps);
				}
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (Exception ex) {
				// ignore
			}
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception e) {
				// ignore
			}
			DBManager.close(conn);
		}
	}

	public static void batchDeleteAndSave(String table, List<String> primarykey, Object[] datalist,
										  List<String> excludes, Object[] deletelist) throws SQLException {
		batchDeleteAndSave(DBManager.getDefaultDatabase(), table, primarykey, datalist, excludes, deletelist);
	}

	public static void batchDelete(String table, List<String> primarykey, List<String> excludes,
								   Object[] deletelist) throws SQLException {
		batchDelete(DBManager.getDefaultDatabase(), table, primarykey, excludes, deletelist);
	}

	public static void batchDelete(String db, String table, List<String> primarykey, List<String> excludes,
								   Object[] deletelist) throws SQLException {
		Connection conn = null;
		try {
			conn = DBManager.getConnection(db);
			conn.setAutoCommit(false);
			// delete
			if (deletelist != null && primarykey != null && primarykey.size() > 0) {
				for (int idx = 0; idx < deletelist.length; ++idx) {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) deletelist[idx];
					if (data.size() == 0) {
						continue; // dangours operation
					}
					Map<String, Object> where = MiscUtil.newArrayMap();
					for (String key : primarykey) {
						if (excludes != null && excludes.contains(key)) {
							continue;
						}
						where.put(key, data.get(key));
					}
					StringBuilder sql = new StringBuilder();
					List<Object> params = MiscUtil.newArrayList();
					sql.append("DELETE FROM `").append(table).append("` ");
					getWhereClause(where, sql, params);
					execute(conn, db, sql.toString(), params.toArray());
				}
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (Exception ex) {
				// ignore
			}
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception e) {
				// ignore
			}
			DBManager.close(conn);
		}
	}

	public static void batchDeleteAndSave(String db, String table, List<String> primarykey,
										  Object[] datalist, List<String> excludes, Object[] deletelist) throws SQLException {
		if (datalist == null) {
			throw new IllegalArgumentException("data = null");
		}
		for (int i = 0; i < datalist.length; ++i) {
			if (!(datalist[i] instanceof Map<?, ?>)) {
				throw new IllegalArgumentException("data components should be Map<String, Object>");
			}
		}
		Connection conn = null;
		try {
			conn = DBManager.getConnection(db);
			conn.setAutoCommit(false);
			// delete
			if (deletelist != null && primarykey != null && primarykey.size() > 0) {
				for (int idx = 0; idx < deletelist.length; ++idx) {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) deletelist[idx];
					if (data.size() == 0) {
						continue; // dangours operation
					}
					Map<String, Object> where = MiscUtil.newArrayMap();
					for (String key : primarykey) {
						if (excludes != null && excludes.contains(key)) {
							continue;
						}
						where.put(key, data.get(key));
					}
					StringBuilder sql = new StringBuilder();
					List<Object> params = MiscUtil.newArrayList();
					sql.append("DELETE FROM `").append(table).append("` ");
					getWhereClause(where, sql, params);
					execute(conn, db, sql.toString(), params.toArray());
				}
			}
			// save
			for (int idx = 0; idx < datalist.length; ++idx) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) datalist[idx];
				if (exists(conn, db, table, data, primarykey)) {
					update(conn, db, table, primarykey, data, excludes);
				} else {
					insertInto(conn, db, table, data, excludes);
				}
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (Exception ex) {
				// ignore
			}
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception e) {
				// ignore
			}
			DBManager.close(conn);
		}
	}

	public static void batchDeleteAndSaveOld(String db, String table, List<String> primarykey,
											 Object[] datalist, List<String> excludes, Object[] deletelist) throws SQLException {
		if (datalist == null) {
			throw new IllegalArgumentException("data = null");
		}
		for (int i = 0; i < datalist.length; ++i) {
			if (!(datalist[i] instanceof Map<?, ?>)) {
				throw new IllegalArgumentException("data components should be Map<String, Object>");
			}
		}
		Connection conn = null;
		try {
			conn = DBManager.getConnection(db);
			conn.setAutoCommit(false);
			// delete
			if (deletelist != null && primarykey != null && primarykey.size() > 0) {
				for (int idx = 0; idx < deletelist.length; ++idx) {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) deletelist[idx];
					if (data.size() == 0) {
						continue; // dangours operation
					}
					Map<String, Object> where = MiscUtil.newArrayMap();
					for (String key : primarykey) {
						if (excludes != null && excludes.contains(key)) {
							continue;
						}
						where.put(key, data.get(key));
					}
					StringBuilder sql = new StringBuilder();
					List<Object> params = MiscUtil.newArrayList();
					sql.append("DELETE FROM `").append(table).append("` ");
					getWhereClause(where, sql, params);
					execute(conn, db, sql.toString(), params.toArray());
				}
			}
			// save
			for (int idx = 0; idx < datalist.length; ++idx) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) datalist[idx];

				StringBuilder sql = new StringBuilder();
				StringBuilder values = new StringBuilder();
				StringBuilder dupldate = new StringBuilder();

				String[] keys = data.keySet().toArray(new String[0]);
				sql.append("INSERT INTO `").append(table).append("` ");
				values.append(" VALUES ");
				dupldate.append(" ON DUPLICATE KEY UPDATE ");

				Object[] params = new Object[keys.length];
				int dIdx = 0;
				for (int i = 0; i < keys.length; ++i) {
					String key = keys[i];

					if (excludes != null && excludes.contains(key)) {
						continue;
					}

					params[i] = data.get(key);
					//
					String delim = (i == 0 ? "(" : ",");
					sql.append(delim);
					values.append(delim);
					//
					sql.append('`').append(key).append('`');
					values.append("?");
					//
					if (!primarykey.contains(key)) {
						if (dIdx > 0) {
							dupldate.append(", ");
						}
						dupldate.append("`").append(key).append("` = VALUES(`");
						dupldate.append(key).append("`)");
						++dIdx;
					}
				}
				sql.append(")");
				values.append(")");
				// values
				sql.append(values.toString());
				sql.append(dupldate.toString());
				// execute
				PreparedStatement ps = null;
				try {
					ps = execute(conn, db, sql.toString(), params);
				} finally {
					DBManager.close(ps);
				}
			}
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (Exception ex) {
				// ignore
			}
			throw e;
		} finally {
			try {
				conn.setAutoCommit(true);
			} catch (Exception e) {
				// ignore
			}
			DBManager.close(conn);
		}
	}
}
