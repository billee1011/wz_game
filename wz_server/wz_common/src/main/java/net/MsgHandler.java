package net;

import net.request.RequestMessage;
import net.response.ResponseMessage;
import server.EServerType;
import sun.security.action.GetBooleanAction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by WZ on 2016/8/25.
 */
public abstract class MsgHandler {
	protected Map<Byte, IRequestHandler> requestHandlers;

	protected interface IRequestHandler {
		public void handler(RequestMessage message);
	}

	public MsgHandler() {
		requestHandlers = new HashMap<>();
		registerAction();
	}

	public abstract void handlerModule();

	public void handler(RequestMessage message) {
		IRequestHandler handler = requestHandlers.get(message.getActionId());
		if (handler == null) {
			System.out.println("action module " + message.getModuleId() + ":" + message.getActionId() + ":"  + EServerType.getByValue(message.getServerId()));
			return;
		}
		handler.handler(message);
	}

	public void registerAction(byte action, IRequestHandler handler) {
		requestHandlers.put(action, handler);
	}

	public abstract void registerAction();

	public abstract int getModule();
}
