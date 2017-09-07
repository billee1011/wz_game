package network.handler.module;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.AbstractHandlers;

/**
 * Created by think on 2017/4/11.
 */
public class AccountModule implements IModuleMessageHandler {
	private static Logger logger = LoggerFactory.getLogger(AccountModule.class);

	private static AtomicInteger order_id = new AtomicInteger(1);

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
	}
	

}
