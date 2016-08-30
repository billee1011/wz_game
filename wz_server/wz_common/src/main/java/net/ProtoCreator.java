package net;

import net.proto.Server.RegisterServer;
import net.proto.Server.DispatchMsg;

/**
 * Created by WZ on 2016/8/25.
 */
public class ProtoCreator {

	public static RegisterServer createRegisterServer(int serverId) {
		RegisterServer.Builder builder = RegisterServer.newBuilder();
		builder.setServerId(serverId);
		return builder.build();
	}

	public static byte[] createRegisterServerBytes(int serverId) {
		return createRegisterServer(serverId).toByteArray();
	}

	public static DispatchMsg createDispatchMsg() {
		DispatchMsg.Builder builder = DispatchMsg.newBuilder();
		builder.setTest1(1);
		builder.setTest2(2);
		builder.setTest3(3);
		builder.setTest4(4);
		return builder.build();
	}

	public static byte[] createDispatchMsgBytes() {
		return createDispatchMsg().toByteArray();
	}

}
