package network;

/**
 * Created by wangfang on 2017/9/15.
 */
public class MessageHolder<MessageLite> {
	private MessageLite t;

	public MessageHolder(MessageLite t) {
		this.t = t;
	}

	public <T> T get() {
		return (T) t;
	}
}
