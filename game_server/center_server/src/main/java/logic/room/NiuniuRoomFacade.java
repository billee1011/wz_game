package logic.room;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;
import config.bean.CoupleRoom;
import define.AppId;
import define.GameType;
import define.constant.LoginStatusConst;
import logic.desk.DeskInfo;
import logic.desk.DeskManager;
import network.ServerManager;
import network.ServerSession;
import packet.CocoPacket;
import proto.Common;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;

/**
 * Created by Administrator on 2017/3/4.
 */
public class NiuniuRoomFacade extends RoomFacade {
	private static final Logger logger = LoggerFactory.getLogger(NiuniuRoomFacade.class);
	
	@Override
	protected boolean checkMoneyValid(Player player, CoupleRoom conf) {
		return true;
	}

	@Override
	protected int getGameNeedPeople() {
		return 0;
	}

	@Override
	protected GameType getGameType() {
		return GameType.NIUNIU;
	}

	@Override
	protected void enterNiuNiuRoom(Player player,int roomId) {
//		if (player.getRoomId() != RoomConst.NIUNIU) {
			int roomNum = addRoomPlayerNum(roomId,player.getPlayerId());
			logger.info("玩家{}进入{},当前房间{}人数{}",player.getPlayerId(),getGameType(),roomId,roomNum);
//		}
		Common.PBPlayerInfo.Builder info = Common.PBPlayerInfo.newBuilder();
		info.setPlayerId(player.getPlayerId());
		info.setCoin((int) player.getCoin());
		info.setName(player.getName());
		info.setIcon(player.getIcon());
		info.setGender(player.getGender().getValue());
		info.setPosition(1);
//		player.setRoomId(RoomConst.NIUNIU);
		info.setProvince(player.getProvince());
		info.setCity(player.getCity());
		info.setIsLeave(player.isLogout());
		info.setChannelId(player.getChannelId());
		info.setPackageId(player.getPackageId());
		info.setDevice(player.getDevice());
		info.setIp(player.getIp());

		DeskInfo deskInfo = DeskManager.getInst().getNiuniuDesk(roomId);
		if(deskInfo == null){
			ServerSession serverSession = DeskManager.getInst().getNiuniuServer();
			if(serverSession == null){
				serverSession = ServerManager.getInst().getNoStopMinLoadSession(AppId.LOGIC);
			}
			if(serverSession == null){
				logger.error("无logic");
				return;
			}
//			serverSession.addLoadFactor(ServerSession.NIU_NIU_LOAD_FACTOR);//让这个服务器变大，因为百人牛牛无人数限制
			deskInfo = DeskManager.getInst().getNorPrivateDesk(null);
			deskInfo.init(getGameType().getValue(),roomId,serverSession.getIoSession());
			DeskManager.getInst().setNiuNiuDesk(deskInfo);
		}
		//百人牛牛客户端需要知道配置
		player.write(ResponseCode.LOBBY_MACHING, CommonCreator.createPBPair(getGameType().getValue(), roomId));
				
		deskInfo.addNoPosPlayer(player);
		
		List<Player> enterPlayerList = new ArrayList<>();
		enterPlayerList.add(player);
	
		deskInfo.getBindServerSession().sendRequest(new CocoPacket(RequestCode.LOGIC_ENTER_NIUNIU_DESK, createPBCreateDesk(getGameType(), roomId, deskInfo.getDeskId(),enterPlayerList), deskInfo.getDeskId()));
		player.updateLoginStatus(roomId + LoginStatusConst.AT_GAME_ROOM_ING);
	}
}
