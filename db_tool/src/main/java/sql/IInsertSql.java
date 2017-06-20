package sql;

import java.util.List;

/**
 * Created by think on 2017/3/28.
 */
public interface IInsertSql extends ISql {

	IInsertSql data(String key, Object value);

	List<Object> getDataParams();
}
