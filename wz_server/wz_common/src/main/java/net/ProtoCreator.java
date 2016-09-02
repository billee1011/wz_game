package net;


import proto.Server;

/**
 * Created by WZ on 2016/8/25.
 */
public class ProtoCreator {

	public static Server.RegisterServer createRegisterServer(int serverId) {
		Server.RegisterServer.Builder builder = Server.RegisterServer.newBuilder();
		builder.setServerId(serverId);
		return builder.build();
	}

	public static byte[] createRegisterServerBytes(int serverId) {
		return createRegisterServer(serverId).toByteArray();
	}


}
