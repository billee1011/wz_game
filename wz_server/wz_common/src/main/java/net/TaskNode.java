package net;

import net.response.ResponseMessage;

/**
 * Created by WZ on 2016/8/25.
 */
public class TaskNode {
	public long sendTime;
	public long recvTime;
	public ResponseMessage message;
	public IResponseHandler response;
	public int seqId;
}
