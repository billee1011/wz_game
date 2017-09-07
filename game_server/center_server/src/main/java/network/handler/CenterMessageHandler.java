package network.handler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import chr.Player;
import chr.PlayerManager;
import define.AppId;
import define.GameType;
import io.netty.channel.ChannelHandlerContext;
import logic.desk.DeskInfo;
import logic.desk.DeskManager;
import logic.desk.GroupDeskManager;
import logic.room.LobbyGameManager;
import network.AbstractHandlers;
import network.NetClient;
import network.ServerManager;
import network.ServerSession;
import network.handler.module.AccountModule;
import network.handler.module.BackendModule;
import network.handler.module.CenterModule;
import network.handler.module.ExchangeModule;
import network.handler.module.GameModule;
import network.handler.module.LobbyModule;
import network.handler.module.MailModule;
import packet.CocoPacket;
import protocol.c2s.RequestCode;
import service.CenterServer;
import util.LogUtil;
import util.Pair;

/**
 * Created by Administrator on 2017/2/6.
 */
public class CenterMessageHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(CenterMessageHandler.class);


	@Override
	protected void registerAction() {
		new AccountModule().registerModuleHandler(this);
		new ExchangeModule().registerModuleHandler(this);
		CenterModule.getIns().registerModuleHandler(this);
		new LobbyModule().registerModuleHandler(this);
		new MailModule().registerModuleHandler(this);
		new BackendModule().registerModuleHandler(this);
		new GameModule().registerModuleHandler(this);
	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		RequestCode reqCode = packet.getReqCode();
		if (reqCode.getSendTo() != getAppId()) {
			if (reqCode.getSendTo() == AppId.LOGIC) {
				Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
				if (player == null) {
					return;
				}
				if (player.getDeskInfo() != null) {
					player.getDeskInfo().writeToLogic(packet);
				}
			} else if (reqCode.getSendTo() == AppId.GATE) {
				Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
				if (player != null) {
					player.write(packet);
				}

			} else {
				ServerManager.getInst().getMinLoadSession(reqCode.getSendTo()).sendRequest(packet);
			}
		} else {
			Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
			if (messageAndHandler == null) {
				logger.warn(" the req code is not handler code: {}, id {} ", reqCode, packet.getReqId());
			} else {
				IActionHandler handler = messageAndHandler.getRight();
				MessageLite protoType = messageAndHandler.getLeft();
				if (handler != null) {
					try {
						final MessageLite message = protoType == null ? null : protoType.getParserForType().parseFrom(packet.getBytes());
						LogUtil.msgLogger.info("player {} , request:{}, packet {}", new Object[]{packet.getPlayerId(), reqCode, message});
						CenterActorManager.getLogicActor(packet.getPlayerId()).put(() -> {
							handler.doAction(client, packet, new MessageHolder<>(message));
							return null;
						});
					} catch (InvalidProtocolBufferException e) {
						logger.debug("exception; {}", e);
					}
				}
			}
		}
	}

	@Override
	protected AppId getAppId() {
		return AppId.CENTER;
	}

	@Override
	protected NetClient getCenterClient() {
		return CenterServer.getInst().getClient();
	}

	@Override
	public void handleSessionInActive(ServerSession session) {
		logger.info("{}服务器断开连接,serverId={},ip={},port={}", session.getAppId(), session.getServerId(), session.getRemoteAddress(), session.getLocalPort());
		if (session.getAppId() == AppId.LOGIC) {
			Collection<Player> players = PlayerManager.getInstance().getOnlinePlayers();

			Set<DeskInfo> set = new HashSet<>();

			Iterator<DeskInfo> it = DeskManager.getInst().getAllDesk().iterator();
			while (it.hasNext()) {
				DeskInfo desk = it.next();
				if (desk.getSessionId() == session.getServerId()) {
					set.add(desk);
				}
			}

			players.forEach(e -> {
				if (e != null && e.getDeskInfo() != null) {
					if (e.getDeskInfo().getSessionId() == session.getServerId()) {
						DeskInfo desk = e.getDeskInfo();
						set.add(desk);
						e.write(new CocoPacket(RequestCode.GATE_KICK_PLAYER, null, e.getPlayerId()));
						if (desk != null) {
							CenterActorManager.getDeskActor().put(() -> {
								LobbyGameManager.getInst().playerLeaveGameRoom(e);
								LobbyGameManager.getInst().playerLeavePriRoom(GameType.getByValue(e.getDeskInfo().getGameId()), e);
								return null;
							});
							e.logout();
						}
					}
				}
			});

			if (set.size() > 0) {
				CenterActorManager.getDeskActor().put(() -> {
					set.forEach(e -> {
						GroupDeskManager.getIns().removeDesk(e);
						DeskManager.getInst().removeDesk(e);
					});
					return null;
				});
			}
		} else if (session.getAppId() == AppId.GATE) {
			Collection<Player> players = PlayerManager.getInstance().getOnlinePlayers();
			players.forEach(e -> {
				if (e.getSession() == session.getIoSession()) {
					CenterModule.getIns().playerLogout(session.getIoSession(), e);
				}
			});
		}
		ServerManager.getInst().removeServerSession(session);
	}
}
