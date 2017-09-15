package manager;

import chr.RyCharacter;
import com.google.protobuf.MessageLite;
import network.MessageHolder;

/**
 * Created by wangfang on 2017/9/15.
 */
public interface IMessageHandler {
	void actionHandle(RyCharacter ch, MessageHolder<MessageLite> IMessageHandler);
}
