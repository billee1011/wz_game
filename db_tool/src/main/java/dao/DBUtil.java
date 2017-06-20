package dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sql.IInsertSql;
import sql.IUpdateSql;
import sql.IWhereSql;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2017/3/27.
 */
public class DBUtil {

    private static final Logger logger = LoggerFactory.getLogger(DBUtil.class);

    public static DataObject executeSingleQuery(IWhereSql sql) {
        List<DataObject> objects = executeQuery(sql);
        if (objects == null || objects.size() == 0) {
            return null;
        }
        if (objects.size() > 1) {
            logger.warn(" get more than one record int single query");
        }
        return objects.get(0);
    }


    public static int executeCount(IWhereSql sql) {
        DataObject data = executeSingleQuery(sql);
        if (data == null) {
            return 0;
        }
        return data.getInt("count(*)", 0);
    }

    public static List<DataObject> executeQuery(IWhereSql sql) {
        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = DBManager.getConnection();
            stat = conn.prepareStatement(sql.build(), Statement.RETURN_GENERATED_KEYS);
            setStatParams(stat, sql.getWhereParams());
            rs = stat.executeQuery();
            return fillQueryResult(rs, stat.getMetaData());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DBManager.close(conn, stat, rs);
        }
    }

    public static int executeUpdate(IUpdateSql sql) {
        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = DBManager.getConnection();
            stat = conn.prepareStatement(sql.build(), Statement.RETURN_GENERATED_KEYS);
            setStatParams(stat, sql.getAllParams());
            return stat.executeUpdate();
        } catch (SQLException e) {
            logger.error("{}", e);
            return -1;
        } finally {
            DBManager.close(conn, stat, rs);
        }
    }

    public static int executeInsert(IInsertSql sql) {
        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = DBManager.getConnection();
            stat = conn.prepareStatement(sql.build(), Statement.RETURN_GENERATED_KEYS);
            setStatParams(stat, sql.getDataParams());
            stat.execute();
            rs = stat.getGeneratedKeys();
            if (rs.next()) {
                return (int) rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("{}", e);
            return 0;
        } finally {
            DBManager.close(conn, stat, rs);
        }
    }

    public static boolean executeDelete(IWhereSql sql) {
        Connection conn = null;
        PreparedStatement stat = null;
        ResultSet rs = null;
        try {
            conn = DBManager.getConnection();
            stat = conn.prepareStatement(sql.build(), Statement.RETURN_GENERATED_KEYS);
            setStatParams(stat, sql.getWhereParams());
            return stat.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBManager.close(conn, stat, rs);
        }
    }

    private static List<DataObject> fillQueryResult(ResultSet rs, ResultSetMetaData meta) throws SQLException {
        List<DataObject> result = new ArrayList<>();
        int cnt = meta.getColumnCount();
        while (rs.next()) {
            DataObject obj = new DataObject();
            for (int i = 0; i < cnt; ++i) {

                String name = meta.getColumnLabel(i + 1);
                Object value = rs.getObject(name);
                if (value instanceof Blob) {
                    //to do  all kind of sql type will be transferred to java type
                } else if (value instanceof Timestamp) {
                    value = ((Timestamp) value).getTime();
                } else if (value instanceof BigInteger) {
                    value = ((BigInteger) value).longValue();
                }
                obj.put(name, value);
            }
            result.add(obj);
        }
        return result;
    }

    private static void setStatParams(PreparedStatement stat, List<Object> params) {
        try {
            for (int i = 0, length = params.size(); i < length; i++) {
                Object param = params.get(i);
                if (param instanceof Integer) {
                    stat.setInt(i + 1, (Integer) param);
                } else if (param instanceof Long) {
                    stat.setLong(i + 1, (Long) param);
                } else if (param instanceof String) {
                    stat.setString(i + 1, (String) param);
                } else if (param instanceof Boolean) {
                    stat.setBoolean(i + 1, (Boolean) param);
                } else if (param instanceof Long) {
                    stat.setLong(i + 1, (Long) param);
                } else {
                    logger.warn(" un handler data type in set param ");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
