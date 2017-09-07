package network.handler.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import cg.PlayerCGManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import common.LogHelper;
import config.CoupleRoomInfoProvider;
import config.bean.CoupleRoom;
import config.bean.PlayerCGConfigData;
import data.OnlineAction;
import database.DataQueryResult;
import db.ProcLogic;
import define.AppId;
import define.GameType;
import define.constant.LoginStatusConst;
import io.netty.channel.ChannelHandlerContext;
import logic.desk.DeskInfo;
import logic.desk.DeskManager;
import logic.desk.GroupDeskManager;
import logic.name.PlayerNameManager;
import logic.room.LobbyGameManager;
import network.AbstractHandlers;
import network.ServerManager;
import network.ServerSession;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.creator.AccountCreator;
import protobuf.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.ASObject;
import util.MiscUtil;
import util.NettyUtil;

public class CenterModule implements IModuleMessageHandler {

	private static final Logger logger = LoggerFactory.getLogger(CenterModule.class);

	private Map<String, LoginInfo> tokenMap = new HashMap<>();

	private static CenterModule ins = new CenterModule();

	public static CenterModule getIns() {
		return ins;
	}

	private CenterModule() {
	}

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.CENTER_REGISTER_SERVER.getValue(), this::actionRegisterServer, Common.PBStringList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_DISPATCH_GATE.getValue(), this::actionDispatchGate, Common.PBStringList.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_LOGIN.getValue(), this::actionValidLogin, Common.PBString.getDefaultInstance());
//		handler.registerAction(RequestCode.CENTER_GATE_PORT.getValue(), this::actionGatePort, Common.PBStringList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_CREATE_DESK_SUCC.getValue(), this::actionCreateDeskSuccess, Common.PBPlayerInfoList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_PLAYER_LOGOUT.getValue(), this::actionPlayerLogout, Common.PBString.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_PLAYER_LEAVE_ROOM.getValue(), this::actionPlayerLeaveRoomSucc);
		handler.registerAction(RequestCode.CENTER_UPDATE_PLAYER_COIN.getValue(), this::actionUpdatePlayerCoin, Common.PBStringList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_CREATE_ROOM_DESK_SUCC.getValue(), this::actionCreateRoomDeskSucc, Common.PBPlayerInfoList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_ROOM_DESK_DISBAND.getValue(), this::actionRoomDeskDisband, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_ROOM_DESK_GAME_END.getValue(), this::actionRoomDeskGameEnd, CoupleMajiang.PBPairGameNo2Record.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_SERVER_PING.getValue(), this::actionServerPing);
		handler.registerAction(RequestCode.CENTER_GMAME_PLAYERS.getValue(), this::actionGamePlayers, Common.PBPairString.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_CREATE_ZJH_DESK_SUCC.getValue(), this::actionCreateZjhDeskSuccess, Common.PBPlayerInfoList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_CREATE_GRAD_NIU_DESK_SUCC.getValue(), this::actionCreateZjhDeskSuccess, Common.PBPlayerInfoList.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_DEBUG_ARRAY_PAI_RES.getValue(), this::clientArrayPaiRes, Common.PBIntString.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_DEBUG_CHECK_FAN_TYPE_RES.getValue(), this::clienCheckFanType, Common.PBIntString.getDefaultInstance());
		handler.registerAction(RequestCode.CENTER_PLAYER_DESK_IS_REMOVE.getValue(), this::playerDeskIsRemove);
		handler.registerAction(RequestCode.CENTER_GATE_FACTOR.getValue(), this::sysnGateFactor, Common.PBInt32.getDefaultInstance());
	}

	private void sysnGateFactor(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		if (packet.getPlayerId() != 1) {
			return;
		}
		Common.PBInt32 request = message.get();
		ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
		if (serverSession != null) {
			serverSession.setFactor(request.getValue());
		}
	}

	private void playerDeskIsRemove(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		player.write(ResponseCode.LOBBY_TICK_PLAYER, CommonCreator.createPBPair(player.getPlayerId(), 0));
	}

	private void clienCheckFanType(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBIntString request = message.get();
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		player.write(ResponseCode.DEBUG_CHECK_FAN_TYPE, request);
	}

	private void clientArrayPaiRes(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBIntString request = message.get();
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		player.write(ResponseCode.DEBUG_ARRAY_PAI, request);
	}

	private void actionCreateZjhDeskSuccess(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBPlayerInfoList request = message.get();
		List<Player> playerList = new ArrayList<>();
		for (Common.PBPlayerInfo info : request.getPlayerInfoListList()) {
			playerList.add(PlayerManager.getInstance().getPlayerById(info.getPlayerId()));
		}
//		ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
//		if (serverSession != null) {
//			serverSession.addLoadFactor();
//		}
		playerList.forEach(e -> {
			e.write(ResponseCode.COUPLE_ENTER_DESK, request);
		});
	}

	private void actionGamePlayers(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBPairString request = message.get();
		Player player = PlayerManager.getInstance().getPlayerById(Integer.valueOf(request.getKey()));
		if (null != player) {
			/// 游戏玩的次数加 1
			player.addGame_total(1);
			/// 记录输贏金额
			long money = Long.valueOf(request.getValue());
			if (0 < money) {
				player.setWin_money(player.getWin_money() + money);
				player.addWin_round(1);
			} else {
				player.setLose_money(player.getLose_money() + money);
				player.addLose_round(1);
			}
			//当日游戏局数加1
			player.addGame_count(1);
			//设置当日游戏时间
			player.addGameTime(MiscUtil.getCurrentSeconds() - player.getLast_game_time());
			player.setLast_game_time(MiscUtil.getCurrentSeconds());
			/// 是否可以打开兑换开关
			if (true == ProcLogic.isOpenExchange(player)) {
				player.write(ResponseCode.ACCOUNT_DYNAMIC_CONFIG, CenterServer.getInst().createPBDynamicConfig(player.getChannelId(), player.getPackageId(), player));
			}

			//发送CG提示
			PlayerCGManager.getInst().sendPlayerCgInfo(player);
		}
	}

	private void actionServerPing(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		ServerSession session = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
		if (session == null) {
			logger.info(" server ping  and session is null");
			return;
		}
	}

	private void actionRoomDeskDisband(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBInt32 request = message.get();
		CenterActorManager.getDeskActor().put(() -> {
			DeskInfo info = DeskManager.getInst().getDeskInfo(request.getValue());
			if (info == null) {
				logger.debug(" the desk {} is not exist", request.getValue());
				return null;
			}

//            ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
//            if (serverSession != null) {
//                serverSession.reduceFactor();
//            }
			info.getPlayerList().forEach(e -> {
				LobbyGameManager.getInst().playerLeavePriRoom(GameType.getByValue(info.getGameId()), e);
				LobbyGameManager.getInst().playerLeaveGameRoom(e);
				logger.info("离开了{}", e.getPlayerId());
				if (e.isLogout()) {
					e.logout();
				}
			});
			info.notifyMessage(ResponseCode.LOBBY_DISBAND_SUCC, null);
			GroupDeskManager.getIns().removeDesk(info);
			DeskManager.getInst().removeDesk(info);
			return null;
		});
	}

	private void actionRoomDeskGameEnd(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		CoupleMajiang.PBPairGameNo2Record request = message.get();
		CoupleMajiang.PBOneRoomRecord record = request.getRecord();
		int deskId = record.getDeskId();
		CenterActorManager.getDeskActor().put(() -> {
			DeskInfo info = DeskManager.getInst().getDeskInfo(deskId);
			if (info == null) {
				logger.debug(" the desk info is null and the desk id is{}", deskId);
				return null;
			}
			info.getPlayerList().forEach(e -> {
				e.addRoomRecordList(request);
				PlayerSaver.savePlayerScore(e, request);
				LobbyGameManager.getInst().playerLeavePriRoom(GameType.getByValue(info.getGameId()), e);
				logger.info("离开了{}", e.getPlayerId());
				if (e.isLogout()) {
					e.logout();
				}
			});
			info.notifyMessage(ResponseCode.ROOM_DESK_GAME_END, record);
			DeskManager.getInst().removeDesk(info);
			return null;
		});
	}


	private void actionCreateRoomDeskSucc(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		int deskId = packet.getPlayerId();
		DeskInfo info = DeskManager.getInst().getDeskInfo(deskId);
		if (info == null) {
			return;
		}

//        ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
//        if (serverSession != null) {
//            serverSession.addLoadFactor();
//        }

		info.setGaming(true);
		info.setIoSession(ioSession);
		info.notifyMessage(ResponseCode.ROOM_CREATE_SUCC, null);
	}

	private void actionUpdatePlayerCoin(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
		if (serverSession == null) {
			return;
		}
		if (serverSession.getAppId() != AppId.LOGIC) {
			//暂时只有logic可以发送这条消息给 center服务器修改玩家金币
			return;
		}
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBStringList request = message.get();
		int coin = Integer.valueOf(request.getList(0));
		boolean add = Integer.valueOf(request.getList(1)) == 1;
		int type = Integer.valueOf(request.getList(2));
		int roomId = Integer.valueOf(request.getList(3));
		boolean isPersonal = Integer.valueOf(request.getList(4)) == 1;

		CenterActorManager.getLogicActor(player.getPlayerId()).put(() -> {
			int db = 1;        /// 积分对应的金额比例 1 倍
			if (0 == type) {
				db = 10;    /// 如果是充值就是 10 倍
			}
			updatePlayerCoin(player, coin, add, db, roomId, isPersonal);
			return null;
		});
	}

	private void updatePlayerCoin(Player player, int coin, boolean add, int db, int roomId, boolean isPersonal) {
	}

	static int PRIVATE_ROOM_ID_LENGTH = 6;

//	// 私房roomid长度为6
//	private boolean isPrivate(int roomId) {
//		return String.valueOf(roomId).length() == PRIVATE_ROOM_ID_LENGTH;
//	}

	private void actionPlayerLeaveRoomSucc(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		CenterActorManager.getDeskActor().put(() -> {
			logger.info("玩家{}离开房间{}成功", player.getPlayerId(), player.getDeskRoomId());
//			if (player.getRoomId() == RoomConst.NIUNIU) {
//				//退出牛牛需要的操作是修改gameI
//				LobbyGameManager.getInst().reduceRoomNum(player);
//				player.setGameId(0);
//				player.setRoomId(0);
//				LobbyGameManager.getInst().playerLeavePriRoom(GameType.NIUNIU, player);
//			} else {
//				LobbyGameManager.getInst().reduceRoomNum(player);
			LobbyGameManager.getInst().playerLeaveGameRoom(player);
			DeskInfo desk = player.getDeskInfo();
			if (desk != null) {
				LobbyGameManager.getInst().playerLeavePriRoom(GameType.getByValue(desk.getGameId()), player);
				desk.removePlayer(player);
				GroupDeskManager.getIns().onPlayerLeaveDesk(player, desk, true);
			}
//			}
			if (player.isLogout()) {
				player.logout();
			}
			return null;
		});
	}

	/**
	 * gate与客户端的连接断开 可能前端 可能后端强制 导致玩家退出
	 */
	private void actionPlayerLogout(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}

		//已经从gate做了挤下线的判断
//		Common.PBString tokenId = message.get();
//		if (!tokenId.getValue().equals(player.getToken())) {
//			logger.info("Player {} is reLogin, do not logout!", player.getPlayerId());
//			ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
//			if (serverSession != null) {
//				serverSession.reduceFactor();
//			}
//			return;
//		}

		playerLogout(ioSession, player);
	}

	public void playerLogout(ChannelHandlerContext ioSession, Player player) {
		CenterActorManager.getDeskActor().put(() -> {
//			if (player.getRoomId() == RoomConst.NIUNIU) {
//				player.setLogout(true);
//				CenterServer.getInst().getNiuniuSession().sendRequest(new CocoPacket(RequestCode.LOBBY_PLAYER_LOGOUT, null, player.getPlayerId()));
//			} else {
			logger.info("玩家{}退出游戏,最后匹配或者房间id{}", player.getPlayerId(), player.getDeskOrMatchRoomId());
			DeskInfo desk = player.getDeskInfo();
			if (desk == null) {
				LobbyGameManager.getInst().playerLeaveGameRoom(player);
				player.logout();
			} else {
				//如果有桌子
				if (!desk.isCreated()) {
					desk.playerLeave(player);
					player.logout();
				} else {
					player.setLogout(true);
					desk.writeToLogic(new CocoPacket(RequestCode.LOBBY_PLAYER_LOGOUT, null, player.getPlayerId()));
				}
			}
//			}
//			ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
//			if (serverSession != null) {
//				serverSession.reduceFactor();
//			}
			return null;
		});
	}

	private void actionCreateDeskSuccess(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBPlayerInfoList request = message.get();
		int deskId = packet.getPlayerId();
		List<Player> playerList = new ArrayList<>();
		for (Common.PBPlayerInfo info : request.getPlayerInfoListList()) {
			playerList.add(PlayerManager.getInstance().getPlayerById(info.getPlayerId()));
		}
		DeskInfo desk = DeskManager.getInst().getDeskInfo(deskId);
//		ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
//		if (serverSession != null) {
//			serverSession.addLoadFactor();
//		}
		desk.writeToClient(ResponseCode.COUPLE_ENTER_DESK, request);
	}

//	private void actionGatePort(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
//		ServerSession session = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
//		if (session == null) {
//			logger.debug(" the  gate session is not exist");
//			return;
//		}
//		Common.PBStringList request = message.get();
//		if (request.getListCount() < 2) {
//			return;
//		}
//		String host = request.getList(0);
//		int port = Integer.parseInt(request.getList(1));
//		session.setLocalPort(port);
//		session.setRemoteAddress(host);
//	}

	private void actionRegisterServer(ChannelHandlerContext ioSession, CocoPacket srcPacket, AbstractHandlers.MessageHolder<MessageLite> message) {
		Common.PBStringList req = message.get();
		ServerSession session = NettyUtil.getAttribute(ioSession, ServerSession.KEY);

		int paramAppId = Integer.valueOf(req.getList(0));
		AppId appId = AppId.getByValue(paramAppId);
		if (appId == null) {
			logger.warn(" the app id is null and the id is {}", paramAppId);
			return;
		}
		session.setAppId(appId);
		long centerStartTime = CenterServer.getInst().getStartTime();
//		if (req.getValue() == 0) {
//			logger.warn(" register the session and the session id is {}", session.getAppId());
//			dynamicId = ServerManager.getInst().registerServer(session);
//		} else {

		ServerManager.getInst().registerServer(centerStartTime, session, req.getListList());
//		}
		int dynamicId = session.getServerId();

		CocoPacket packet = new CocoPacket(RequestCode.CENTER_REGISTER_SERVER.getValue(), CommonCreator.createPBStringList(dynamicId + "", centerStartTime + ""));
		packet.setSeqId(srcPacket.getSeqId());
		ioSession.writeAndFlush(packet);
	}

	private void actionDispatchGate(ChannelHandlerContext ioSession, CocoPacket srcPacket, AbstractHandlers.MessageHolder<MessageLite> message) {
		ServerSession session = ServerManager.getInst().getMinLoadSession(AppId.GATE);
		if (session == null) {
			logger.warn("没有gate可以分配");
			return;
		}
		String gateIp = session.getRemoteAddress();
		int gatePort = session.getLocalPort();
		Common.PBStringList request = message.get();
		String[] strs = request.getList(0).split(":");
		int userId = Integer.parseInt(strs[0]);
		LoginInfo info = new LoginInfo();
		info.userId = userId;
		tokenMap.put(strs[1], info);
		logger.info(" the gate ip is {} and the port is {}", gateIp, gatePort);
		CocoPacket packet = new CocoPacket(RequestCode.CENTER_DISPATCH_GATE, CommonCreator.createPBString(gateIp + ":" + gatePort));
		packet.setSeqId(srcPacket.getSeqId());
		ioSession.writeAndFlush(packet);
	}

	private void onPlayerOtherLogin(Player player) {
		//如果被挤下线未在logic创建的私房+桌子组匹配房直接离开
		DeskInfo desk = player.getDeskInfo();
		if (desk != null && !desk.isCreated()) {
			desk.onPlayerOtherLogin(player);
		}
	}

	private void actionValidLogin(ChannelHandlerContext ioSession, CocoPacket srcPacket, AbstractHandlers.MessageHolder<MessageLite> message) {
		logger.info("player login the game ");
		Common.PBString request = message.get();
		if (request == null) {
			return;
		}
		String token = request.getValue();
		LoginInfo info = tokenMap.get(token);
		if (info == null) {
			logger.warn("the token is not success");
			return;
		}
		Map<String, Object> playerWhere = new HashMap<>();
		playerWhere.put("user_id", info.userId);

		CenterActorManager.getDbActor(info.userId).put(() -> DataQueryResult.load("player", playerWhere), g -> {
			@SuppressWarnings("unchecked")
			List<ASObject> playerList = (List<ASObject>) g;
//			ServerManager.getInst().addServerSessionLoadFactor(ioSession);
			if (playerList.size() == 0) {
				Player player = Player.getDefault(info.userId, PlayerNameManager.getInst().randomName4NewPlayer());
				info.copyPlayer(player);
				int playerId = PlayerSaver.insertPlayer(player);
				player.setPlayerId(playerId);
				logger.info("new account so create player for it and the player id is {}", playerId);
//				DataManager.getInst().loadChar(playerId, (e, f) -> {
//					if (e != null) {
//						onPlayerLoginSucc(ioSession, srcPacket, e, info, token);
//					} else {
//						logger.error(" load char failed because {}", f);
//					}
//				});
				onPlayerLoginSucc(ioSession, srcPacket, player, info, token);
			} else {
				ASObject data = playerList.get(0);
				int playerId = data.getInt("player_id");
				CenterActorManager.getLogicActor(playerId).put(() -> {
					logger.info("load player {} from cache or database ", playerId);
					Player player = PlayerManager.getInstance().getPlayerById(playerId);
//					if (player != null) {
					logger.info("player is on line so kick out the current player {}", playerId);
					if (player.getLoginStatus() == LoginStatusConst.EXIT_GAME) {
						onPlayerLoginSucc(ioSession, srcPacket, player, info, token);
					} else {
//							String preToken = player.getToken();
						player.setToken(token);
						info.copyPlayer(player);
						player.setLoginTime(MiscUtil.getCurrentSeconds());

						if (player.isLogout()) {
							player.setLogout(false);
						} else {
							ChannelHandlerContext preIoSession = player.getSession();
							if (preIoSession != null) {
								ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
								ServerSession preServerSession = NettyUtil.getAttribute(preIoSession, ServerSession.KEY);
								//如果之前在不同的gate,踢下线,且退出之后不需要往center发送掉线消息
								if (serverSession != preServerSession) {
									player.write(ResponseCode.ACCOUNT_LOGIN_OTHER_WHERE, null);
								}
//									//同个gate在gate返回时处理
//									if (preServerSession != null) {
//										preServerSession.reduceFactor();
//									}
								onPlayerOtherLogin(player);
								logger.info("Player {} is reLogin, do not logout! curGate{}", player.getPlayerId(), serverSession.getServerId());
							}
						}

						CocoPacket packet = new CocoPacket(ResponseCode.ACCOUNT_LOGIN_SUCC.getValue(), AccountCreator.createPBLoginAndDynamic(player));
						packet.setSeqId(srcPacket.getSeqId());
						ioSession.writeAndFlush(packet);

						player.setSession(ioSession);
						//推送CG
						PlayerCGConfigData cgConfigData = PlayerCGManager.getInst().getTheMaxCG(player);
						if (cgConfigData != null) {
							player.write(ResponseCode.PLAYRE_CG, CommonCreator.createPBPairString(cgConfigData.getLimit_detail(), cgConfigData.getForward_url()));
						}
					}
//					} else {
//						DataManager.getInst().loadChar(playerId, (e, f) -> {
//							if (e != null) {
//								onPlayerLoginSucc(ioSession, srcPacket, e, info, token);
//							} else {
//								logger.error(" load char failed because {}", f);
//							}
//						});
//					}
					PlayerManager.getInstance().registerPlayer(player);
					return null;
				});

			}
		}, CenterActorManager.getLogicActor(0));
	}

	private void onPlayerLoginSucc(ChannelHandlerContext ioSession, CocoPacket srcPacket, Player player, LoginInfo info, String token) {
//		CharData charData = (CharData) data;
//		Player player = Player.getDefault();
		player.setToken(token);
		player.setSession(ioSession);
		player.setLogout(false);
//		PlayerLoader.loadFromCharData(charData, player);
		info.copyPlayer(player);
		player.setLoginTime(MiscUtil.getCurrentSeconds());

		PlayerManager.getInstance().registerPlayer(player);
		player.updateLoginStatus(LoginStatusConst.ENTER_HALL);

		CocoPacket packet = new CocoPacket(ResponseCode.ACCOUNT_LOGIN_SUCC.getValue(), AccountCreator.createPBLoginAndDynamic(player));
		packet.setSeqId(srcPacket.getSeqId());
		ioSession.writeAndFlush(packet);
		logger.info("player {} login success ", player.getPlayerId());
		ServerManager.getInst().getMinLoadSession(AppId.LOG)
				.sendRequest(new CocoPacket(RequestCode.LOG_ONLINE
						, LogHelper.logOnline(player.getPlayerId(), player.getChannelId(), player.getPackageId(), OnlineAction.LOGIN.getValue()
						, info.ip, info.province, info.city, info.device, info.machine_id)));
		PlayerCGManager.getInst().loadAlreadyCgInfo(player);
		//推送CG
		PlayerCGConfigData cgConfigData = PlayerCGManager.getInst().getTheMaxCG(player);
		if (cgConfigData != null) {
			player.write(ResponseCode.PLAYRE_CG, CommonCreator.createPBPairString(cgConfigData.getLimit_detail(), cgConfigData.getForward_url()));
		}
	}

	private class LoginInfo {
		public int userId;
		public String province;
		public String city;
		public String channel;
		public String device;
		public int packageId;
		public String ip;
		public String machine_id;
		public String game_version;
		public String app_version;
		public String platform_id;

		public void copyPlayer(Player player) {
			player.setProvince(this.province);
			player.setCity(this.city);
			player.setGameChannel(this.channel);
			player.setPackageId(this.packageId);
			player.setDevice(this.device);
			player.setIp(this.ip);
			player.setMachine_id(this.machine_id);
			player.setGame_version(this.game_version);
			player.setApp_version(this.app_version);
			player.setPlatform_id(platform_id);
		}
	}
}
