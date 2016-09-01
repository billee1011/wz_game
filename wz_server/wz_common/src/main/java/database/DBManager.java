package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.WzProperties;

public class DBManager {

	private static final Logger logger = LoggerFactory.getLogger(DBManager.class);

	private static DBManager instance = new DBManager();

	private WzProperties props;

	private DBManager(){

	}

	public static DBManager getInst(){
		return instance;
	}

	public void initDbProperties(WzProperties props){
		this.props = props;
	}

	public Connection getConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String url = props.getString("account.db.url" , "");
		String userName = props.getString("account.db.user" , "");
		String password = "";
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url , userName  , password );
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
}
