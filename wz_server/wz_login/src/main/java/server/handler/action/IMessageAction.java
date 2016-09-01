package server.handler.action;

import com.google.protobuf.MessageLite;

/**
 * Created by WZ on 2016/9/1 0001.
 */
public abstract class IMessageAction {

	public abstract boolean valid(MessageLite message);

	public void handler(MessageLite message) {
		if( !valid(message)) {
			return;
		}
		handlerMessage(message);
	}

	public abstract void handlerMessage(MessageLite message);
}
