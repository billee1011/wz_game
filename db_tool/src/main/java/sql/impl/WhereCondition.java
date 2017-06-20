package sql.impl;

import sql.define.EWhereType;

/**
 * Created by think on 2017/3/27.
 */
public class WhereCondition {
	private String key;

	private Object value;

	private EWhereType whereType;

	public WhereCondition(String key, Object value, EWhereType whereType) {
		this.key = key;
		this.value = value;
		this.whereType = whereType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public EWhereType getWhereType() {
		return whereType;
	}

	public void setWhereType(EWhereType whereType) {
		this.whereType = whereType;
	}
}
