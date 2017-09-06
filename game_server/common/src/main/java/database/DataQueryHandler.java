package database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DataQueryHandler implements IQueryHandler {

    Logger logger = LoggerFactory.getLogger(DataQueryHandler.class);

    private Object data;

    public void onError(SQLException e) {
        setData(e);
        logger.error("", e);
    }

    public abstract void onResult(PreparedStatement ps) throws SQLException;

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public boolean isException() {
        return data != null && data instanceof Exception;
    }

}
