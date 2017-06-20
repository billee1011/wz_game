package sql;

import sql.define.EWhereType;
import sql.impl.WhereCondition;

import java.util.List;

/**
 * Created by think on 2017/3/27.
 */
public interface IWhereSql extends ISql {

	IWhereSql where(WhereCondition... conditions);

	List<Object> getWhereParams();

	IWhereSql where(WhereCondition condition);

	default IWhereSql where(String key, Object value, EWhereType whereType) {
		return where(new WhereCondition(key, value, whereType));
	}


}
