package network.handler.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import chr.Player;
import chr.PlayerManager;
import config.CoupleRoomInfoProvider;
import config.bean.CoupleRoom;
import config.provider.PaoMaDengProvider;
import database.DataQueryResult;
import db.ProcLogic;
import define.AppId;
import define.GameType;
import define.constant.LoginStatusConst;
import define.constant.MessageConst;
import io.netty.channel.ChannelHandlerContext;
import logic.desk.DeskInfo;
import logic.desk.DeskManager;
import logic.room.LobbyGameManager;
import network.AbstractHandlers;
import network.ServerManager;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.Lobby;
import protobuf.creator.CommonCreator;
import protobuf.creator.LobbyCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import rank.RankManager;
import util.ASObject;
import util.MiscUtil;

/**
 * Created by think on 2017/4/11.
 */
public class LobbyModule implements IModuleMessageHandler {
	private static Logger logger = LoggerFactory.getLogger(LobbyModule.class);


	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.LOBBY_ENTER_GAME.getValue(), this::actionEnterGame, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_ENTER_GAME_ROOM.getValue(), this::actionEnterGameRoom, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_LEAVE_DESK.getValue(), this::actionPlayerLeaveRoom);
		handler.registerAction(RequestCode.LOBBY_CREATE_ROOM_DESK.getValue(), this::actionCreateRoomDesk, Lobby.PBCreateDeskReq.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_ENTER_ROOM_DESK.getValue(), this::actionEnterRoomDesk, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_GET_ROOM_INFO.getValue(), this::actionGetRoomInfo, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_TRANS_VOICE.getValue(), this::actionTransVoice, Common.PBIntString.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_DISBAND_DESK.getValue(), this::actionRequestDisbandDesk);
		handler.registerAction(RequestCode.LOBBY_AGREE_DISBAND.getValue(), this::actionAgreeDisbandDesk);
		handler.registerAction(RequestCode.LOBBY_REFUSE_DISBAND.getValue(), this::actionRefuseDisbandDesk);
		handler.registerAction(RequestCode.LOBBY_GET_PEOPLE_NUM.getValue(), this::actionGetPeopleNum, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_ENTER_MODULE.getValue(), this::actionEnterModule, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_EXIT_MODULE.getValue(), this::actionExitModule);
		handler.registerAction(RequestCode.LOBBY_GET_ROOM_SCORE_LIST.getValue(), this::actionGetRoomScoreList);
		handler.registerAction(RequestCode.LOBBY_GET_SCORE_DETAIL.getValue(), this::actionGetRoomDetail, Common.PBString.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_RANK_INFO.getValue(), this::actionRankInfo);
		handler.registerAction(RequestCode.GAME_RECORD.getValue(), this::gameRecord, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_ZJH_BEGIN_GAME.getValue(), this::beginGame);
		handler.registerAction(RequestCode.LOBBY_DEBUG_ARRAY_PAI.getValue(),this::clientArrayPai,Common.PBIntString.getDefaultInstance());
		handler.registerAction(RequestCode.LOBBY_DEBUG_CHECK_FAN_TYPE.getValue(),this::clientCheckFanType,Common.PBIntListList.getDefaultInstance());
	}
	
	private void clientCheckFanType(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBIntListList array = message.get();
		CocoPacket sendPacket = new CocoPacket(RequestCode.LOGIC_DEBUG_CHECK_FAN_TYPE, array, packet.getPlayerId());
		ServerManager.getInst().getMinLoadSession(AppId.LOGIC).sendRequest(sendPacket);
	}
	
	private void clientArrayPai(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBIntString array = message.get();
		CocoPacket sendPacket = new CocoPacket(RequestCode.LOGIC_DEBUG_ARRAY_PAI, array, packet.getPlayerId());
		ServerManager.getInst().getMinLoadSession(AppId.LOGIC).sendRequest(sendPacket);
	}

	private void beginGame(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		CenterActorManager.getDeskActor().put(() -> {
			DeskInfo deskInfo = player.getDeskInfo();
			if (deskInfo == null) {
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DESK_DESTROY));
				return null;
			}
			synchronized (deskInfo) {
				if(deskInfo.isCreated()){//已经创建了桌
					return null;
				}
				if(!deskInfo.isPrivateRoom()){
					logger.info("不是私房房间{}",deskInfo.getDeskId());
					return null;
				}
				if (!deskInfo.canCreateDesk()) {
					logger.info("房间{}需要人数太少{}，不能进行游戏",deskInfo.getDeskId(),deskInfo.getPlayerList().size());
					return null;
				}
				CocoPacket sendPacket = new CocoPacket(RequestCode.LOGIC_CREATE_ROOM_DESK, deskInfo.createRoomReq());
				ServerManager.getInst().getNoStopMinLoadSession(AppId.LOGIC).sendRequest(sendPacket);
			}
			return null;
		});
	}

	private void gameRecord(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		int record = request.getValue();
		List<ASObject> rankList = DataQueryResult.load("select * from game_record where game_id=" + record);
		if (rankList.size() > 0) {
			ASObject object = rankList.get(0);
			String game_text = object.getString("game_text");
			player.write(ResponseCode.LOBBY_GAME_RECORD, CommonCreator.createPBString(game_text));
		} else {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.RECORD_NO_EXIST));
		}
	}

	private void actionRankInfo(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		RankManager.getInst().getRankItemList(player);
	}


	private void actionGetRoomDetail(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBString req = message.get();
		long gameNo = Long.parseLong(req.getValue());
		player.write(ResponseCode.ROOM_SCORE_DETAIL, player.getRecordByGameNo(gameNo));
	}


	private void actionGetRoomScoreList(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		List<CoupleMajiang.PBPairGameNo2Record> recordList = player.getRecordList();
		CoupleMajiang.PBTotalRoomRecord.Builder builder = CoupleMajiang.PBTotalRoomRecord.newBuilder();
		long time_dot = MiscUtil.getTimerPreDot(7, 24L);
		if (recordList != null) {
			recordList.forEach(e -> {
				if (e.getRecord().getDeskCreateTime() >= time_dot) {
					builder.addRecordList(createPBGameNoRecord(e));
				}
			});
		}
		player.write(ResponseCode.LOBBY_GET_ROOM_SCORE_LIST, builder.build());
	}


	private CoupleMajiang.PBGameNoRecord createPBGameNoRecord(CoupleMajiang.PBPairGameNo2Record record) {
		CoupleMajiang.PBGameNoRecord.Builder builder = CoupleMajiang.PBGameNoRecord.newBuilder();
		builder.setGameNo(String.valueOf(record.getGameNo()));
		CoupleMajiang.PBOneRoomRecord oneRoomRecord = record.getRecord();
		CoupleMajiang.PBOneRoomTotalRecord.Builder subBuilder = CoupleMajiang.PBOneRoomTotalRecord.newBuilder();
		subBuilder.setGameId(oneRoomRecord.getGameId());
		subBuilder.setDeskCreateTime(oneRoomRecord.getDeskCreateTime());
		subBuilder.setDeskId(oneRoomRecord.getDeskId());
		subBuilder.setCreatorId(oneRoomRecord.getCreateId());
		Map<Integer, Integer> result = new HashMap<>();
		Map<Integer, String> pos2Name = new HashMap<>();
		subBuilder.setRounds(oneRoomRecord.getRecordListCount());
		for (CoupleMajiang.PBOneGameRecord oneRecord : oneRoomRecord.getRecordListList()) {
			for (CoupleMajiang.PBOnePosInfo subRecord : oneRecord.getInfoListList()) {
				pos2Name.put(subRecord.getPos(), subRecord.getName());
				Integer originalValue = result.get(subRecord.getPos());
				if (originalValue == null) {
					result.put(subRecord.getPos(), subRecord.getScore());
				} else {
					result.put(subRecord.getPos(), subRecord.getScore() + originalValue);
				}
			}
		}
		for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
			CoupleMajiang.PBOnePosInfo.Builder posBuilder = CoupleMajiang.PBOnePosInfo.newBuilder();
			posBuilder.setPos(entry.getKey());
			posBuilder.setScore(entry.getValue());
			posBuilder.setName(pos2Name.get(entry.getKey()));
			subBuilder.addScoreList(posBuilder.build());
		}
		builder.setRecord(subBuilder.build());
		return builder.build();
	}

	private void actionExitModule(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		LobbyGameManager.getInst().playerExitModule(player);
//		player.write(ResponseCode.LOBBY_PLAYER_MODULE_NUM
//				, CommonCreator.createPBPairList(LobbyGameManager.getInst().getModulePlayerNum()));
	}


	private void actionEnterModule(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		LobbyGameManager.getInst().playerEnterModule(player, request.getValue());
		player.write(ResponseCode.LOBBY_PLAYER_ENTER_MODULE_SUCC, request);
	}


	private void sendPaoMaDeng(Player player) {
		List<ASObject> list_object = PaoMaDengProvider.getInst().getData(1, player.getPlayerId(), 10);
		/// 发送走马灯
		if (null == list_object || list_object.size() == 0) {
			return;
		}
		player.write(ResponseCode.LOBBY_PAO_MA_DENG, LobbyCreator.createPBPaomadengList(list_object, 1));
	}

	private void actionGetPeopleNum(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		
		Common.PBInt32 request = message.get();
		Map<Integer, Integer> mapState = LobbyGameManager.getInst().getGameModelState(request.getValue());
		player.write(ResponseCode.LOBBY_PLAYER_MODULE_NUM, LobbyCreator.createPBGetPlayerNumRes(request.getValue(),mapState));
		
//		player.write(ResponseCode.LOBBY_PLAYER_MODULE_NUM
//				, CommonCreator.createPBPairList(LobbyGameManager.getInst().getModulePlayerNum()));

		/// 发送跑马灯
		sendPaoMaDeng(player);
		/// 是否发送充值邮件
		ProcLogic.procPayMail(player.getPlayerId(), 0); 
		
//		DeskInfo desk = player.getDeskInfo();
//		if (desk != null) {
//			player.write(ResponseCode.LOBBY_IN_GAME, CommonCreator.createPBPair(desk.getGameId(), desk.getRoomId()));
//		}
	}

	private void actionRefuseDisbandDesk(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		DeskInfo info = player.getDeskInfo();
		if (info == null) {
			logger.debug(" you is not in a room ");
			return;
		}
		if(!info.isPrivateRoom()){
			return;
		}
		info.playerRefuseDisband(player);
	}


	private void actionAgreeDisbandDesk(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		DeskInfo info = player.getDeskInfo();
		if (info == null) {
			logger.debug(" you is not in a room ");
			return;
		}
		if(!info.isPrivateRoom()){
			return;
		}
		info.playerAgreeDisband(player);
	}

	private void actionRequestDisbandDesk(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		DeskInfo info = player.getDeskInfo();
		if (info == null) {
			logger.debug(" you is not in a room ");
			return;
		}
		if(!info.isPrivateRoom()){
			return;
		}
		info.playerWantDisband(player);
	}

	private void actionTransVoice(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		DeskInfo info = player.getDeskInfo();
		if (info == null) {
			logger.debug(" you is not in a room ");
			return;
		}
		info.notifyMessage(ResponseCode.LOBBY_TRANS_VOICE, message.get());
	}

	private void actionGetRoomInfo(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		DeskInfo info = DeskManager.getInst().getDeskInfo(request.getValue());
		if (info == null) {
			logger.debug(" the room is  not  exist {} ", request.getValue());
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DESK_ID_ERROR));
			return;
		}
		if (!info.isPrivateRoom()) {
			logger.debug(" the room is  not  privateRoom {} ", request.getValue());
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DESK_ID_ERROR));
			return;
		}
		
		player.write(ResponseCode.LOBBY_GET_ROOM_DESK_INFO, info.createDeskInfo());
	}

	/** 加入私房 */
	private void actionEnterRoomDesk(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		CenterActorManager.getDeskActor().put(() -> {
			if (player.getMatchingRoomId() != 0) {
				//自动退出匹配队列
				LobbyGameManager.getInst().playerLeaveGameRoom(player);
			}
			DeskInfo deskInfo = player.getDeskInfo();
			if (deskInfo != null) {
				player.write(ResponseCode.LOBBY_IN_GAME, CommonCreator.createPBPair(deskInfo.getGameId(), deskInfo.getRoomId()));
				return null;
			}
			Common.PBInt32 request = message.get();
			deskInfo = DeskManager.getInst().getDeskInfo(request.getValue());
			if (deskInfo == null) {
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DESK_DESTROY));
				return null;
			}
//		if (!deskInfo.playerMoneyEnough(player)) {
//			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(10068));
//			return;
//		}
			if (player.getCoin() < deskInfo.getCheckMoneyNum()) {
				player.write(ResponseCode.MESSAGE_PARAME, CommonCreator.createPBIntStringList(10068, String.valueOf(deskInfo.getCheckMoneyNum() / 100)));
				return null;
			}
			if (deskInfo.isFull()) {
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.DESK_FULL));
				return null;
			}
			if(!deskInfo.isPrivateRoom()){
				logger.error("非私房间，{}不能加入 {} ", player.getPlayerId(),request.getValue());
				return null;
			}
			if(deskInfo.isAtDesk(player.getPlayerId())){
				logger.error("已经在房间内，{}不能加入 {} ", player.getPlayerId(),request.getValue());
				return null;
			}
			
			if(deskInfo.isCreated()){
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.GAME_STARTED));
				logger.error("游戏已经开始{}不能加入 {}", player.getPlayerId(),request.getValue());
				return null;
			}
			LobbyGameManager.getInst().playerEnterPriRoom(GameType.getByValue(deskInfo.getGameId()), player);
			deskInfo.addPosPlayer(player);
			player.write(ResponseCode.LOBBY_MACHING, CommonCreator.createPBPair(deskInfo.getGameId(), deskInfo.getDeskId()));
			deskInfo.notifyPlayerEnter();
			synchronized (deskInfo) {
				//当人数满足基本游戏需求时，开始游戏
				if (deskInfo.isFull() && !deskInfo.isCreated()) {
					CocoPacket sendPacket = new CocoPacket(RequestCode.LOGIC_CREATE_ROOM_DESK, deskInfo.createRoomReq());
					ServerManager.getInst().getNoStopMinLoadSession(AppId.LOGIC).sendRequest(sendPacket);
				}
			}
			return null;
		});
	}

	/** 创建私房 */
	private void actionCreateRoomDesk(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		CenterActorManager.getDeskActor().put(() -> {
			if (player.getMatchingRoomId() != 0) {
				//自动退出匹配队列
				LobbyGameManager.getInst().playerLeaveGameRoom(player);
			}
			DeskInfo desk = player.getDeskInfo();
			if (desk != null) {
				player.write(ResponseCode.LOBBY_IN_GAME, CommonCreator.createPBPair(desk.getGameId(), desk.getRoomId()));
				return null;
			}
			Lobby.PBCreateDeskReq req = message.get();
			GameType gameType = GameType.getByValue(req.getGameId());
			long needCoin = 0;
			boolean paramError = false;
			switch (gameType) {
				case XUENIU:
				case XUEZHAN:
					Lobby.PBCreateXueNiuReq xueNiuReq = req.getRequest();
					needCoin = xueNiuReq.getEnterTimes() * xueNiuReq.getBaseScore();
					if(xueNiuReq.getBaseScore() <= 0){
						paramError = true;
					}
					if(xueNiuReq.getEnterTimes() < 200){
						paramError = true;
					}
					break;
				case ZJH: // ZJh
					Lobby.PBCreateZjhReq zjhReq = req.getZjhReq();
					needCoin = zjhReq.getLimitPots() * zjhReq.getEnterPots();
					if(zjhReq.getLimitPots() <= 0){
						paramError = true;
					}
					if(zjhReq.getEnterPots() < 200){
						paramError = true;
					}
					break;
				case CLASS_NIU:
					Lobby.PBCreateNiuReq reqNiu = req.getNiuReq();
					needCoin = reqNiu.getBaseScore() * reqNiu.getEnterTimes();
					if(reqNiu.getBaseScore() <= 0){
						paramError = true;
					}
					if(reqNiu.getEnterTimes() < 200){
						paramError = true;
					}
					if(reqNiu.getModel() != 1 && reqNiu.getModel() != 2){
						paramError = true;
					}
					if(reqNiu.getRule() != 1 && reqNiu.getRule() != 2 && reqNiu.getRule() != 3){
						paramError = true;
					}
					
					if(reqNiu.getRule() == 2 || reqNiu.getRule() == 3){
						if(reqNiu.getMulit() != 2 && reqNiu.getMulit() != 3 && reqNiu.getMulit() != 4){
							paramError = true;
						}
					}
					break;
				default:
					logger.error("此类{}游戏,未校验参数合法性,玩家{}",req.getGameId(),player.getPlayerId());
					return null;
			}
			if(paramError){
				logger.error("创建私房参数错误{}",req);
				return null;
			}
			if (player.getCoin() < needCoin) {
//			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(10068));
				player.write(ResponseCode.MESSAGE_PARAME, CommonCreator.createPBIntStringList(10068, String.valueOf(needCoin / 100)));
				return null;
			}
			desk = DeskManager.getInst().getPrivateDesk();
			desk.setGameId(gameType.getValue());
			desk.setRoomId(0);
			desk.setCreator(player);
			desk.addPosPlayer(player);
			desk.setReq(req);
			player.write(ResponseCode.LOBBY_MACHING, CommonCreator.createPBPair(desk.getGameId(), desk.getDeskId()));
			desk.notifyPlayerEnter();
			LobbyGameManager.getInst().playerEnterPriRoom(GameType.getByValue(req.getGameId()), player);
			return null;
		});
	}

	private void actionPlayerLeaveRoom(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		CenterActorManager.getDeskActor().put(() -> {
			logger.info("玩家{}离开房间{}", player.getPlayerId(), player.getDeskOrMatchRoomId());
//			if (player.getRoomId() == RoomConst.NIUNIU) {
//				actionLeaveNiuniuDesk(player);
//			} else {
				DeskInfo desk = player.getDeskInfo();
				if (desk == null) {
					LobbyGameManager.getInst().playerLeaveGameRoom(player);
				} else {
					desk.playerLeave(player);
				}
//			}
			return null;
		});
	}

//	private void actionLeaveNiuniuDesk(Player player) {
//		CenterServer.getInst().getNiuniuSession().sendRequest(new CocoPacket(RequestCode.LOGIC_PLAYER_LEAVE_DESK, null, player.getPlayerId()));
//		player.updateLoginStatus(LoginStatusConst.ENTER_GAME_ING);
//	}

	private void actionEnterGameRoom(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		logger.info("player {} enter room {}", player.getName(), request.getValue());

		int roomId = request.getValue();
		CoupleRoom coupleRoom =  CoupleRoomInfoProvider.getInst().getRoomConf(roomId);
		
		int stauts = coupleRoom.getStatus();
		if(stauts == 0){
			logger.info("player {} enter room {} , but this room no open !", player.getName(), request.getValue());
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.SERVER_STOP));
			return;
		}
		
		int gameId = coupleRoom.getMode();
		
		CenterActorManager.getDeskActor().put(() -> {
			if (player.getMatchingRoomId() != 0) {
//			player.write(ResponseCode.LOBBY_IN_GAME, CommonCreator.createPBPair(player.getGameId(), player.getRoomId()));
				//自动退出匹配队列
				LobbyGameManager.getInst().playerLeaveGameRoom(player);
			}
//			if (player.getRoomId() == RoomConst.NIUNIU) {
//				player.write(ResponseCode.LOBBY_IN_GAME, CommonCreator.createPBPair(player.getGameId(), player.getRoomId()));
//				return null;
//			}
			DeskInfo desk = player.getDeskInfo();
			if (desk != null) {
				player.write(ResponseCode.LOBBY_IN_GAME, CommonCreator.createPBPair(desk.getGameId(), desk.getRoomId()));
				return null;
			}
			
			player.updateLoginStatus(LoginStatusConst.AT_ENTER_MATCH + request.getValue());
			LobbyGameManager.getInst().playerEnterRoom(player,gameId, request.getValue());
			return null;
		});
	}

	private void actionEnterGame(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBInt32 request = message.get();
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		logger.info(" player {} enter game {}", player.getPlayerId(), GameType.getByValue(request.getValue()));
//		player.setGameId(request.getValue());
//		if (GameType.getByValue(request.getValue()) == GameType.NIUNIU) {
//			/// 判断是否开启
//			int room_status = CoupleRoomInfoProvider.getInst().getRoomStatus(RoomConst.NIUNIU);
//			if (0 == room_status) {
//				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.SERVER_STOP));
//				logger.info("actionEnterGame room id {} status : {}",RoomConst.NIUNIU, room_status);
//				return;
//			}
//			CenterActorManager.getDeskActor().put(() -> {
//				if (player.getMatchingRoomId() != 0) {
//					//自动退出匹配队列
//					LobbyGameManager.getInst().playerLeaveGameRoom(player);
//				}
//				DeskInfo desk = player.getDeskInfo();
//				if (desk != null) {
//					player.write(ResponseCode.LOBBY_IN_GAME, CommonCreator.createPBPair(desk.getGameId(), desk.getRoomId()));
//					return null;
//				}
////				player.setGameId(GameType.NIUNIU.getValue());
//				LobbyGameManager.getInst().playerEnterRoom(player,GameType.NIUNIU.getValue(), RoomConst.NIUNIU);
//				return null;
//			});
//		} else {
			Map<Integer, Integer> mapState = LobbyGameManager.getInst().getRoomPlayerList_ex(request.getValue());
			if (0 == mapState.size()) {
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.SERVER_STOP));
				logger.info("actionEnterGame room info {} num : {}", request.getValue(), mapState.size());
				return;
			}
			player.write(ResponseCode.LOBBY_GAME_ROOM_PLAYER_NUM, CommonCreator.createPBPairList(mapState));
//		}
	}
}