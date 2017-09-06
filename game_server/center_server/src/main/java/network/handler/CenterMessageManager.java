package network.handler;

import network.AbstractHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/2/4.
 */
public class CenterMessageManager {
	private static final Logger logger = LoggerFactory.getLogger(CenterMessageManager.class);

	private static CenterMessageManager instance = new CenterMessageManager();


	private CenterMessageManager() {

	}

	private AbstractHandlers handlers = new CenterMessageHandler();

	public static CenterMessageManager getInst() {
		return instance;
	}


}
