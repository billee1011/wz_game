package logic.desk;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;
import config.CoupleRoomInfoProvider;
import config.bean.CoupleRoom;
import define.AppId;
import define.constant.LoginStatusConst;
import network.ServerManager;
import network.ServerSession;
import packet.CocoPacket;
import proto.Common;
import proto.CoupleMajiang;
import protocol.c2s.RequestCode;

public class GroupDesk extends DeskInfo {
	private static Logger logger = LoggerFactory.getLogger(GroupDesk.class);

	public GroupDesk(int deskId) {
		super(deskId);
	}

	public void addPlayer(Player p, int roomId) {
		addPosPlayer(p);

		if (isCreated()) {
			// 通知logic 桌子玩家加入
			logger.info("通知logic玩家{}加入到 "+getGameName()+" 桌子{},当前人数为:{}", p.getPlayerId(), this.getDeskId(), getSize());

			Common.PBPlayerInfo.Builder info = Common.PBPlayerInfo.newBuilder();
			info.setPlayerId(p.getPlayerId());
			info.setCoin((int) p.getCoin());
			info.setName(p.getName());
			info.setIcon(p.getIcon());
			info.setGender(p.getGender().getValue());
			info.setPosition(getPosition(p));
			info.setProvince(p.getProvince());
			info.setCity(p.getCity());
			info.setIsLeave(p.isLogout());
			info.setChannelId(p.getChannelId());
			info.setPackageId(p.getPackageId());
			info.setDevice(p.getDevice());
			info.setIp(p.getIp());
			getBindServerSession()
					.sendRequest(new CocoPacket(RequestCode.LOGIC_ENTER_GRAB_NIU_DESK, info.build(), getDeskId()));
			p.updateLoginStatus(roomId + LoginStatusConst.AT_GAME_ROOM_ING);
		} else {
			synchronized (this) {
				// 若未创建，如果size>=2,就通知logic创建桌子
				if (canCreateDesk() && !isCreated()) {
					StringBuffer sb = new StringBuffer();
					sb.append("通知logic创建 "+getGameName()+" 桌子").append(this.getDeskId()).append(",玩家个数为:").append(getSize());
					// 通知logic 创建桌子
					getPlayerList().forEach(e -> {
						e.updateLoginStatus(roomId + LoginStatusConst.AT_GAME_ROOM_ING);
						sb.append(", ").append(e.getPlayerId());
					});
					logger.info(sb.toString());
					CocoPacket packet = new CocoPacket(RequestCode.LOGIC_CREATE_DESK,
							createPBCreateDesk(roomId, getDeskId(), getPlayerList()));
					ServerSession session = ServerManager.getInst().getNoStopMinLoadSession(AppId.LOGIC);
					if(session != null){
						setIoSession(session.getIoSession());
						session.sendRequest(packet);
					}else{
						logger.error("无logic服务器");
					}
				}
			}
		}
	}

	private CoupleMajiang.PBCreateDesk createPBCreateDesk(int roomId, int deskId,
			List<Player> playerList) {
		CoupleMajiang.PBCreateDesk.Builder builder = CoupleMajiang.PBCreateDesk.newBuilder();
		builder.setGameType(getGameId());
		builder.setRoomId(roomId);
		builder.setDeskId(deskId);
		Common.PBPlayerInfoList.Builder playerBuilder = Common.PBPlayerInfoList.newBuilder();
		CoupleRoom room = CoupleRoomInfoProvider.getInst().getRoomConf(roomId);
		playerList.forEach(e -> {
			Common.PBPlayerInfo.Builder info = Common.PBPlayerInfo.newBuilder();
			info.setPlayerId(e.getPlayerId());
			if (2 == room.getClassify()) { // tiyan room and dont't save this
											// game record
				info.setCoin(e.getTiyanCoin());
			} else {
				info.setCoin((int) e.getCoin());
			}
			info.setName(e.getName());
			info.setIcon(e.getIcon());
			info.setGender(e.getGender().getValue());
			info.setPosition(getPosition(e));
			info.setCity(e.getCity());
			info.setProvince(e.getProvince());
			info.setIsLeave(e.isLogout());
			info.setChannelId(e.getChannelId());
			info.setPackageId(e.getPackageId());
			info.setDevice(e.getDevice());
			info.setIp(e.getIp());

			playerBuilder.addPlayerInfoList(info);
		});
		builder.setPlayerList(playerBuilder);
		return builder.build();
	}
}