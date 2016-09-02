package client;

import database.DBUtil;
import database.DbObject;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * Created by Administrator on 2016/8/29 0029.
 */
public class LoginClient {
	public static final String CLIENT_KEY = "client";

	private ChannelHandlerContext ioSession;

	private String userName;

	private String password;

	private int userId;

	public LoginClient(ChannelHandlerContext context) {
		this.ioSession = context;
	}

	public ChannelHandlerContext getIoSession() {
		return ioSession;
	}

	public void setIoSession(ChannelHandlerContext ioSession) {
		this.ioSession = ioSession;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public void disconnect() {
		ioSession.channel().close();
	}


	public List<LoginCharacter> loadCharacterList() {
		List<LoginCharacter> charList = null;
		DbObject where = new DbObject();
		where.put("user_id", userId);
		List<DbObject> result = DBUtil.executeQuery("player", where);
		result.forEach( e ->{
			LoginCharacter character = new LoginCharacter(e);
			charList.add(character);
		});
		return charList;
	}
}
