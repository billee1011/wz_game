package sql.impl;

import sql.IInsertSql;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2017/3/28.
 */
public class InsertSql implements IInsertSql {
    private List<Pair<String, Object>> dataList;

    private StringBuilder builder;

    private String tableName;

    public InsertSql(String tableName) {
        this.tableName = tableName;
        this.builder = new StringBuilder();
    }

    @Override
    public List<Object> getDataParams() {
        List<Object> result = new ArrayList<>();
        if (dataList != null) {
            int size = dataList.size();
            for (int i = 0; i < size; i++) {
                result.add(dataList.get(i).getRight());
            }
        }
        return result;
    }

    @Override
    public String build() {
        builder.append("insert into ");
        builder.append(tableName);
        int size = dataList.size();
        builder.append("(");
        for (int i = 0; i < size; i++) {
            builder.append(dataList.get(i).getLeft());
            if (i != size - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        builder.append(" values ");
        builder.append("(");
        for (int i = 0; i < size; i++) {
            builder.append("?");
            if (i != size - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        // key list  and set param list;
        return builder.toString();
    }

    @Override
    public IInsertSql data(String key, Object value) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        dataList.add(new Pair<>(key, value));
        return this;
    }


}
