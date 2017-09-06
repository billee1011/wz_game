package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IntDataQueryHandler extends DataQueryHandler {

    @Override
    public void onResult(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.getResultSet();
        if (rs.next()) {
            int cnt = rs.getInt(1);
            setData(cnt);
        }
        else {
            setData(0);
        }
    }

    @Override
    public void onError(SQLException e) {
        setData(e);
    }
}
