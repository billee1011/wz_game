package net.handler;

import com.sun.org.apache.regexp.internal.REUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.IResponseHandler;
import net.MsgHandler;
import net.TaskNode;
import net.request.RequestMessage;
import net.response.ResponseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by WZ on 2016/8/25.
 */
public abstract class ClientHandler extends ChannelInboundHandlerAdapter {
	private static final int REQUEST_FLAG = 0x10000000;

	private ChannelHandlerContext outSession;

	private Map<Byte, MsgHandler> handlerMap;

	private Map<Integer, TaskNode> taskMap;

	private AtomicInteger sequence = null;

	public ClientHandler() {
		handlerMap = new HashMap<>();
		taskMap = new HashMap<>();
		sequence = new AtomicInteger(1000);
		registerMsgHandler();
	}

	public void sendRequest(RequestMessage message, IResponseHandler handler) {
		if (outSession == null || handler == null) {

		} else {
			TaskNode node = new TaskNode();
			node.sendTime = System.currentTimeMillis();
			node.response = handler;
			node.seqId = getSequenceId() + REQUEST_FLAG;
			registerTask(node.seqId, node);
		}
		message.setSeqId(getSequenceId() + REQUEST_FLAG);
		outSession.writeAndFlush(message.getBuffer());
	}

	public void sendRequest(RequestMessage message) {
		sendRequest(message, null);
	}


	private void registerTask(int seqId, TaskNode node) {
		taskMap.put(seqId, node);
	}

	private void releaseTask(int seqId) {
		taskMap.remove(seqId);
	}

	private int getSequenceId() {
		return sequence.getAndIncrement();
	}

	public abstract void registerMsgHandler();

	public void registerModuleHandler(byte module, MsgHandler handler) {
		handlerMap.put(module, handler);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof RequestMessage) {
			RequestMessage request = (RequestMessage) msg;
			request.setSeqId(request.getSeqId() - 0x10000000);
			MsgHandler handler = handlerMap.get(request.getModuleId());
			if (handler != null) {
				handler.handler(request);
			}
		}
		if (msg instanceof ResponseMessage) {
			ResponseMessage response = (ResponseMessage) msg;
			TaskNode node = taskMap.get(response.getSeqId());
			node.message = response;
			node.response.callback(node);
			releaseTask(node.seqId);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		outSession = ctx;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

	}


	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	}
}
