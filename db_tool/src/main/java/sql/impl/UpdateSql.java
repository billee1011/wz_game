package sql.impl;

import sql.IUpdateSql;
import util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by think on 2017/3/28.
 */
public class UpdateSql extends AbstractWhereSql implements IUpdateSql {
    private List<Pair<String, Object>> dataList;


    public UpdateSql(String tableName) {
        super(tableName);
    }

    @Override
    public String build() {
        builder.append("update ");
        builder.append(tableName);
        int size = dataList.size();
        builder.append(" set ");
        for (int i = 0; i < size; i++) {
            builder.append(dataList.get(i).getLeft());
            builder.append(" = ? ");
            if (i != size - 1) {
                builder.append(",");
            }
        }
        builder.append(getWhereClause());
        return builder.toString();
    }

    @Override
    public IUpdateSql data(String key, Object value) {
        if (dataList == null) {
            dataList = new ArrayList<>();
        }
        dataList.add(new Pair<>(key, value));
        return this;
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
    public IUpdateSql where(WhereCondition... conditions) {
        int length = conditions.length;
        for (int i = 0; i < length; i++) {
            if (conditionList == null) {
                conditionList = new ArrayList<>();
            }
            conditionList.add(conditions[i]);
        }
        return this;
    }

    @Override
    public IUpdateSql where(WhereCondition condition) {
        if (conditionList == null) {
            conditionList = new ArrayList<>();
        }
        conditionList.add(condition);
        return this;
    }

    @Override
    public List<Object> getAllParams() {
        List<Object> list1 = getDataParams();
        List<Object> list2 = getWhereParams();
        int size1 = list1.size();
        int size2 = list2.size();
        Object[] array = new Object[size1 + size2];
        System.arraycopy(list1.toArray(), 0, array, 0, size1);
        System.arraycopy(list2.toArray(), 0, array, size1, size2);
        return Arrays.asList(array);
    }
}
