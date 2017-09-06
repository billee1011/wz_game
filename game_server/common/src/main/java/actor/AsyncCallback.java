package actor;

import java.util.concurrent.Semaphore;

public class AsyncCallback implements ICallback {

    public enum CallbackStatus {
        NONE, // 
        OK, // 
    }

    private Semaphore semaphore = new Semaphore(0, true);

    private Object resultData = null;

    private CallbackStatus callbackStatus = CallbackStatus.NONE;

    private final ICallback callback;

    /**
     * 等待响应
     */
    public CallbackStatus blockUntilResponse() {
        semaphore.acquireUninterruptibly();
        return callbackStatus;
    }
    
    public Object getResultUntilResponse() {
        blockUntilResponse();
        return getResult();
    }

    public Object getResult() {
        return this.resultData;
    }

    protected void releaseSemaphore() {
        semaphore.release();
    }

    public AsyncCallback() {
        this.callback = null;
    }

    public AsyncCallback(ICallback callback) {
        this.callback = callback;
    }

    @Override
    public void onResult(Object result) {
        this.resultData = result;
        callbackStatus = CallbackStatus.OK;
        if (this.callback != null) {
            this.callback.onResult(resultData);
        }
        releaseSemaphore();
    }

}
