package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBResult {
    private Map<String, Integer> names = null;
    private List<Object[]> data = null;
    private long generatedKey = 0;
    private int index = -1;

    public boolean next() {
        ++index;
        return data != null && index < data.size();
    }

    public Object getObject(String name) {
        if (data == null) {
            return null;
        }
        if (index >= data.size()) {
            return null;
        }
        if (!names.containsKey(name)) {
            return null;
        }
        int i = names.get(name);
        return data.get(index)[i];
    }

    public DBResult(PreparedStatement ps) throws SQLException {
        ResultSet resSet = ps.getResultSet();
        if (resSet != null) {
            data = new ArrayList<Object[]>();
            while (resSet.next()) {
                if (names == null) {
                    names = new HashMap<String, Integer>();
                    for (int i = 0; i < resSet.getMetaData().getColumnCount(); ++i) {
                        String name = resSet.getMetaData().getColumnName(i);
                        names.put(name, i);
                    }
                }
                Object[] entry = new Object[names.size()];
                data.add(entry);
                for (int i = 0; i < resSet.getMetaData().getColumnCount(); ++i) {
                    String name = resSet.getMetaData().getColumnName(i);
                    entry[i] = resSet.getObject(name);
                }
            }
        }
        else {
            data = null;
        }

        // key
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            setGeneratedKey(rs.getLong(1));
        }
        else {
            setGeneratedKey(0);
        }
    }

    public void setGeneratedKey(long generatedKey) {
        this.generatedKey = generatedKey;
    }

    public long getGeneratedKey() {
        return generatedKey;
    }
}
