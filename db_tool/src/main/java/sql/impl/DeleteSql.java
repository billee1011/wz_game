package sql.impl;

/**
 * Created by think on 2017/3/28.
 */
public class DeleteSql extends AbstractWhereSql {

	public DeleteSql(String tableName) {
		super(tableName);
	}

	@Override
	public String build() {
		builder.append("delete from ");
		builder.append(tableName);
		builder.append(getWhereClause());
		return builder.toString();
	}
}
