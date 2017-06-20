package sql.impl;

import sql.IWhereSql;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by think on 2017/3/27.
 */
public abstract class AbstractWhereSql implements IWhereSql {

	protected StringBuilder builder;

	protected String tableName;

	protected List<WhereCondition> conditionList;


	public AbstractWhereSql(String tableName) {
		this.builder = new StringBuilder();
		this.tableName = tableName;
		this.conditionList = null;
	}

	protected void checkArgument() {
		if (tableName == null) {
			throw new RuntimeException("table name must have a non-null value");
		}
	}

	@Override
	public List<Object> getWhereParams() {
		List<Object> result = new ArrayList<>();
		int size = conditionList.size();
		for (int i = 0; i < size; i++) {
			result.add(conditionList.get(i).getValue());
		}
		return result;
	}

	protected String getWhereClause() {
		StringBuilder builder = new StringBuilder();
		if (conditionList != null) {
			int size = conditionList.size();
			for (int i = 0; i < size; i++) {
				if (i == 0) {
					builder.append(" where ");
				} else {
					builder.append(" and ");
				}
				builder.append(conditionList.get(i).getKey());
				builder.append(" ");
				builder.append(conditionList.get(i).getWhereType().getSignal());
				builder.append(" ? ");
			}
		}
		return builder.toString();
	}


	@Override
	public IWhereSql where(WhereCondition... conditions) {
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
	public IWhereSql where(WhereCondition condition) {
		if (conditionList == null) {
			conditionList = new ArrayList<>();
		}
		conditionList.add(condition);
		return this;
	}
}
