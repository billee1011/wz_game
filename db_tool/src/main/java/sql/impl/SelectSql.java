package sql.impl;

import sql.IWhereSql;

/**
 * Created by think on 2017/3/28.
 */
public class SelectSql extends AbstractWhereSql {


	private String[] selectCols;

	public SelectSql(String tableName) {
		super(tableName);
	}


	protected void checkArgument() {
		super.checkArgument();
	}


	public IWhereSql select(String... keys) {
		this.selectCols = keys;
		return this;
	}

	@Override
	public String build() {
		checkArgument();
		if (selectCols == null) {
			builder.append("select * ");
		} else {
			builder.append("select ");
			int length = selectCols.length;
			for (int i = 0; i < length; i++) {
				builder.append(selectCols[i]);
				if (i != length - 1) {
					builder.append("");
					builder.append(",");
				}
			}
			builder.append("");
		}
		builder.append(" from ");
		builder.append(tableName);
		builder.append(getWhereClause());
		String result = builder.toString();
		System.out.println(result);
		return result;
	}
}
