package network.handler.module;

import chr.Player;
import chr.PlayerManager;
import com.google.protobuf.MessageLite;
import io.netty.channel.ChannelHandlerContext;
import logic.desk.DeskInfo;
import network.AbstractHandlers;
import packet.CocoPacket;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;

/**
 * Created by think on 2017/4/11.
 */
public class GameModule implements IModuleMessageHandler {
	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.XUENIU_GET_DISBAND_INFO.getValue(), this::actionGetRoomDisbandInfo);
	}

	private void actionGetRoomDisbandInfo(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		DeskInfo info = player.getDeskInfo();
		if (info == null) {
			return;
		}
		player.write(ResponseCode.XUENIU_DISBAND_INFO, info.createPBDisbandInfo());
	}
}
