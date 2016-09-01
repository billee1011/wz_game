package server.handler.action;

import com.google.protobuf.MessageLite;
import proto.Server;

/**
 * Created by WZ on 2016/9/1 0001.
 */
public class LoginPasswordAction extends   IMessageAction{

	@Override
	public boolean valid(MessageLite message) {
		return message instanceof Server.AccountLogin;
	}

	@Override
	public void handlerMessage(MessageLite message) {
		Server.AccountLogin request = (Server.AccountLogin)message;
		String userName = request.getUsername();
		String password = request.getPassword();
	}
}
