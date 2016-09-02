package server.handler.action;

import client.LoginClient;
import com.google.protobuf.MessageLite;

/**
 * Created by WZ on 2016/9/1 0001.
 */
public abstract class IMessageAction {

	protected abstract boolean valid(MessageLite message);

	public void handler(LoginClient client, MessageLite message) {
		if (!valid(message)) {
			return;
		}
		handlerMessage(client, message);
	}

	protected abstract void handlerMessage(LoginClient client, MessageLite message);
}
