package database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface IQueryHandler {

    void onError(SQLException e);

    void onResult(PreparedStatement ps) throws SQLException;
}
