package server.handler.msg;

import net.MsgHandler;
import net.request.RequestMessage;

/**
 * Created by WZ on 2016/8/26.
 */
public class MsgOneHandler extends MsgHandler {
	@Override
	public void handlerModule() {

	}

	@Override
	public void registerAction() {
		registerAction((byte) 1, MsgOneHandler::justTest);
	}

	public static void justTest(RequestMessage message) {
		System.out.println(message.toString());
	}


	@Override
	public int getModule() {
		return 0;
	}
}
