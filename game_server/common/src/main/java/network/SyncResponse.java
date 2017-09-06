package network;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actor.ICallback;

/**
 * Created by Administrator on 2017/2/6.
 */
public class SyncResponse implements ICallback {
	private static final Logger logger = LoggerFactory.getLogger(SyncResponse.class);
	private Semaphore semaphore = new Semaphore(0, true);

	private ICallback handler;

	/**
	 * 等待响应
	 */
	public void blockUntilResponse() {
		boolean suc = false;
		try {
			suc = semaphore.tryAcquire(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("同步請求異常！！！");
		}
		if(!suc){
			logger.error("同步請求超時！！！");
		}
	}

	protected void releaseSemaphore() {
		semaphore.release();
	}

	public SyncResponse(ICallback handler) {
		this.handler = handler;
	}


	@Override
	public void onResult(Object result) {
		if (this.handler != null) {
			this.handler.onResult(result);
		}
		releaseSemaphore();
	}
}
