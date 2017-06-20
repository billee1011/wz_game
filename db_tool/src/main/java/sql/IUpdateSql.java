package sql;

import sql.define.EWhereType;
import sql.impl.WhereCondition;

import java.util.List;

/**
 * Created by think on 2017/3/28.
 */
public interface IUpdateSql extends IWhereSql, IInsertSql {

	@Override
	IInsertSql data(String key, Object value);

	@Override
	IWhereSql where(WhereCondition... conditions);

	@Override
	IUpdateSql where(WhereCondition condition);

	@Override
	default IUpdateSql where(String key, Object value, EWhereType whereType) {
		return where(new WhereCondition(key, value, whereType));
	}

	List<Object> getAllParams();
}
