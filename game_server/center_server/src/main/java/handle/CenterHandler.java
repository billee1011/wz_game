package handle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import actor.CenterActorManager;
import cg.PlayerCGManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import common.LogHelper;
import config.CoupleRoomInfoProvider;
import config.DynamicInfoProvider;
import config.bean.ChannelConfig;
import config.bean.PlayerAgentRequest;
import config.bean.TransferData;
import config.provider.AgentAuickReplyProvider;
import config.provider.AgentInfoProvider;
import config.provider.AnnouncementProvider;
import config.provider.ChannelInfoProvider;
import config.provider.ConfNiuProvider;
import config.provider.ConfPlayerExceptionProvider;
import config.provider.ConfServerStateProvider;
import config.provider.DynamicPropertiesPublicProvider;
import config.provider.ExchangeBigLimitProvider;
import config.provider.PaoMaDengProvider;
import config.provider.PlayerCGConfigProvider;
import config.provider.ProvinceProvider;
import config.provider.RankInfoProvider;
import data.BankAction;
import data.MoneyAction;
import data.MoneySubAction;
import database.DBUtil;
import database.DataQueryResult;
import db.ProcLogic;
import define.AppId;
import define.GameType;
import define.constant.MessageConst;
import logic.desk.DeskInfo;
import logic.desk.DeskManager;
import logic.desk.GroupDesk;
import logic.desk.GroupDeskManager;
import logic.room.LobbyGameManager;
import mail.MailEntity;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import network.ServerManager;
import network.ServerSession;
import network.handler.module.CenterModule;
import packet.CocoPacket;
import protobuf.creator.AccountCreator;
import protobuf.creator.CommonCreator;
import protobuf.creator.LobbyCreator;
import protobuf.creator.MailCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.ASObject;
import util.LogUtil;
import util.MiscUtil;
import util.Pair;

/**
 * Created by Administrator on 2017/2/25.
 */
public class CenterHandler extends AbstractHandler {
	private static Logger logger = LoggerFactory.getLogger(CenterHandler.class);

	@Override
	public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
		LogUtil.logHttp(logger, s, request);

		if (s.equals("/rank_reward")) {
			handleRankReward(httpServletRequest, httpServletResponse);
		} else if (s.equals("/conf_room")) {
			handleConfRoom(httpServletRequest, httpServletResponse);
		} else if (s.equals("/msg_feedback")) {
			handleMsgFeedBack(httpServletRequest, httpServletResponse);
		} else if (s.equals("/recharge")) {
			handleRecharge(httpServletRequest, httpServletResponse);
		} else if (s.equals("/exchange_callback")) {
			handleExchangeCallback(httpServletRequest, httpServletResponse);
		} else if (s.equals("/dynamic_change")) {
			handleDynamicChange(httpServletRequest, httpServletResponse);
		} else if (s.equals("/paomadeng")) {
			handlePaomadeng(httpServletRequest, httpServletResponse);
		} else if (s.equals("/conf_channel_switch")) {
			handleConfigChannelSwitch(httpServletRequest, httpServletResponse);
		} else if (s.equals("/reload_announcement")) {    // 刷新公告与区域配置
			handleAnnouncement(httpServletRequest, httpServletResponse);
		} else if (s.equals("/unbind_alipay")) {
			handleUnbindAlipay(httpServletRequest, httpServletResponse);
		} else if (s.equals("/block")) {
			handleBlockPlayer(httpServletRequest, httpServletResponse);
		} else if (s.equals("/web_coin")) {
			handleWebCoin(httpServletRequest, httpServletResponse);
		} else if (s.equals("/updatePayParam")) {
			handleUpdatePayParam(httpServletRequest, httpServletResponse);
		} else if (s.equals("/conf_player_exception")) {
			handlePlayerConfException(httpServletRequest, httpServletResponse);
		} else if (s.equals("/main_send")) {
			handleMainSend(httpServletRequest, httpServletResponse);
		} else if (s.equals("/agent_info")) {
			handleAgentInfo(httpServletRequest, httpServletResponse);
		} else if (s.equals("/agent_info_db")) {
			handleAgentInfoDb(httpServletRequest, httpServletResponse);
		} else if (s.equals("/agent_pay")) {
			handleAgentPay(httpServletRequest, httpServletResponse);
		} else if (s.equals("/update_agent_bank_coin")) {
			handleUpdateAgentBankCoin(httpServletRequest, httpServletResponse);
		} else if (s.equals("/return_orderid_pay")) {
			handleReturnOrderidPay(httpServletRequest, httpServletResponse);
		} else if (s.equals("/exchange_restore")) {
			handleExchangeRestore(httpServletRequest, httpServletResponse);
		} else if (s.equals("/exchange_big_limit")) {
			handleExchangeBigLimit(httpServletRequest, httpServletResponse);
		} else if (s.equals("/only_show_agent")) {
			PlayerOnlyShowAgentHandler(httpServletRequest, httpServletResponse);
		} else if (s.equals("/agent_auick_reply")) {
			AgentAuickReplyHandler(httpServletRequest, httpServletResponse);
		} else if (s.equals("/agent_plan_update")) {
			AgentPlanUpdateHandler(httpServletRequest, httpServletResponse);
		} else if (s.equals("/cg_config")) {
			CGConfigHandler(httpServletRequest, httpServletResponse);
		} else if (s.equals("/agent_hand_reply")) {
			AgentHandReplyHandler(httpServletRequest, httpServletResponse);
		} else if (s.equals("/web_rankpwd")) {
			handleWebRankPWD(httpServletRequest, httpServletResponse);
		} else if (s.equals("/conf_personal_room")) {
			handleConfPersonalRoom(httpServletRequest, httpServletResponse);
		} else if (s.equals("/test")) {
			handleTest(httpServletRequest, httpServletResponse);
		} else if (s.equals("/queryAllGameInfo")) {
			handleQueryAllGameInfo(httpServletRequest, httpServletResponse);
		} else if (s.equals("/removeDesk")) {
			handleRemoveDesk(httpServletRequest, httpServletResponse);
		} else if (s.equals("/queryPlayerDesk")) {
			handleQueryPlayerDesk(httpServletRequest, httpServletResponse);
		} else if (s.equals("/queryDesk")) {
			handleQueryDesk(httpServletRequest, httpServletResponse);
		} else if (s.equals("/queryLogcDeskNum")) {
			handleQueryLogcDeskInfo(httpServletRequest, httpServletResponse);
		} else if (s.equals("/queryServersInfo")) {
			handleServersInfo(httpServletRequest, httpServletResponse);
		} else if (s.equals("/removeServer")) {
			handleRemoveServer(httpServletRequest, httpServletResponse);
		} else if (s.equals("/loadSlbInfo")) {
			handleLoadSlbInfo(httpServletRequest, httpServletResponse);
		} else if (s.equals("/upatePro")) {
			handleUpdatePro(httpServletRequest, httpServletResponse);
		} else if (s.equals("/loadServerState")) {
			handleLoadServerState(httpServletRequest, httpServletResponse);
		} else if (s.equals("/queryDeskList")) {
			handleQueryDeskList(httpServletRequest, httpServletResponse);
		} else if (s.equals("/refresh_dynamic_public")) {
			handleRefreshDynamicPublic(httpServletRequest, httpServletResponse);
		}
	}

	/**
	 * 通知登陆服刷新配置
	 */
	private void noticeLoginReloadConf() {
		List<ServerSession> sessionList = ServerManager.getInst().getSessionList(AppId.LOGIN);
		if (sessionList != null && sessionList.size() > 0) {
			sessionList.forEach(e -> e.sendRequest(new CocoPacket(RequestCode.LOGIN_RELOAD_CONF, null)));
		}
	}

	private void handleRefreshDynamicPublic(HttpServletRequest request, HttpServletResponse response) {
		DynamicPropertiesPublicProvider.getInst().reLoad();
		for (Player player : PlayerManager.getInstance().getOnlinePlayers()) {
			player.write(ResponseCode.ACCOUNT_DYNAMIC_CONFIG,
					CenterServer.getInst().createPBDynamicConfig(player.getChannelId(), player.getPackageId(), player));
		}
		noticeLoginReloadConf();
	}

	private void handleQueryDeskList(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Semaphore sp = new Semaphore(0, true);
		CenterActorManager.getDeskActor().put(() -> {
			Iterator<DeskInfo> it = DeskManager.getInst().getAllDesk().iterator();
			JSONArray deskList = new JSONArray();
			while (it.hasNext()) {
				DeskInfo desk = it.next();
				JSONObject deskObj = new JSONObject();
				deskObj.put("gameId", desk.getGameId());
				deskObj.put("roomId", desk.getRoomId());
				deskObj.put("deskId", desk.getDeskId());
				List<Player> playerList = new ArrayList<Player>(desk.getPlayerList());
				JSONArray playerListObj = new JSONArray();
				for (Player p : playerList) {
					JSONObject playerObj = new JSONObject();
					playerObj.put("playerId", p.getPlayerId());
					playerObj.put("loginStatus", p.getLoginStatus());
					playerListObj.add(playerObj);
				}
				deskObj.put("players", playerListObj);
				deskList.add(deskObj);
			}
			writeResponse(httpServletResponse, deskList.toString());
			sp.release();
			return null;
		});
		try {
			sp.tryAcquire(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			LogUtil.error(logger, e);
		}
	}

	private void handleLoadServerState(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");

		ConfServerStateProvider.getInst().reLoad();
	}

	private void handleUpdatePro(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

		int type = Integer.parseInt(httpServletRequest.getParameter("type"));
		int playerId = Integer.parseInt(httpServletRequest.getParameter("param1"));
		String param2 = httpServletRequest.getParameter("param2");

		CenterActorManager.getLogicActor(playerId).put(() -> {
			Player player = PlayerManager.getInstance().getPlayerById(playerId);
			if (null != player) {
				switch (type) {
					case 3:         /// 支付宝 名修改
						player.setAlipayName(param2);
						break;
					case 4:         /// 支付宝 账号修改
						player.setAlipayAccount(param2);
						break;
				}
				PlayerSaver.savePlayerAlipay(player);
			}
			player.write(ResponseCode.ACCOUNT_UPDATE_DATA, AccountCreator.createPBLoginSucc(player));
			return null;
		});
		writeResponse(httpServletResponse, "200");
	}

	private void handleLoadSlbInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");

		CenterActorManager.getDbCheckActor().put(() -> {
			return null;
		});
	}

	private void handleRemoveServer(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String paramApp = httpServletRequest.getParameter("app");
		String paramEndTime = httpServletRequest.getParameter("endTime");//2017-07-02 21:00:00

		int endTime = MiscUtil.getSecondsOfTimeStamp_ex(paramEndTime, "yyyy-MM-dd hh:mm:ss");
		List<ASObject> list_object = new ArrayList<>();
		ASObject object = new ASObject();
		object.put("timeFrom", MiscUtil.getCurrentSeconds());
		object.put("timeTo", endTime);
		object.put("content", "服务器将在" + paramEndTime + " 停服，请各位玩家局时下线，以免发生不要的损失");
		object.put("delay", 10);
		object.put("id", 10000);
		list_object.add(object);

		AppId app = AppId.getByDesc(paramApp);
		if (app == AppId.CENTER) {
			writeResponse(httpServletResponse, "200");

			List<ServerSession> sessionList = ServerManager.getInst().getSessionList(AppId.GATE);
			if (sessionList != null) {
				sessionList.forEach(e -> e.sendRequest(new CocoPacket(RequestCode.GATE_BROAD_CAST_MESSAGE
						, CommonCreator.createPBGameProtocol(ResponseCode.LOBBY_PAO_MA_DENG.getValue()
						, LobbyCreator.createPBPaomadengList(list_object, 1).toByteArray()))));
			}

			CenterServer.getInst().beginStop(endTime);
			return;
		}

		int serverId = Integer.parseInt(httpServletRequest.getParameter("serverId"));

		ServerSession serverSession = ServerManager.getInst().getServerSession(app, serverId);
		serverSession.getStop().set(true);

		if (app == AppId.LOGIC) {
			//不能再加入
			CenterActorManager.getDeskActor().put(() -> {

				Iterator<DeskInfo> it = DeskManager.getInst().getAllDesk().iterator();
				while (it.hasNext()) {
					DeskInfo desk = it.next();
					if (desk.getSessionId() == serverSession.getServerId()) {
						logger.info("维护 " + desk.getGameName() + " 房间 {}", desk.getRoomId());
						if (desk.getGameId() == GameType.NIUNIU.getValue()) {
							DeskManager.getInst().resetNiuNiuDeskMap();
						}
						if (desk instanceof GroupDesk) {
							GroupDeskManager.getIns().deskStop((GroupDesk) desk);
						}
						List<Player> players = new ArrayList<>(desk.getPlayerList());
						for (Player player : players) {
							player.write(ResponseCode.LOBBY_PAO_MA_DENG, LobbyCreator.createPBPaomadengList(list_object, 1));
						}

					}
				}
				return null;
			});
		}

		//指定时间后关闭
		CenterServer.getInst().sendStopServerRequest(serverSession, endTime);
		writeResponse(httpServletResponse, "200");
	}

	private void handleServersInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		JSONArray serverApps = new JSONArray();
		for (AppId appId : AppId.values()) {
			List<ServerSession> sessionList = ServerManager.getInst().getSessionList(appId);
			if (sessionList != null && sessionList.size() > 0) {
				JSONArray serverApp = new JSONArray();
				for (ServerSession session : sessionList) {
					JSONObject server = new JSONObject();
					server.put("app", appId.getDesc());
					server.put("serverId", session.getServerId());
					server.put("Address", session.getRemoteAddress() + ":" + session.getRemotePort());
					server.put("loadFactor", session.getLoadFactor());

					serverApp.add(server);
				}
				serverApps.add(serverApp);
			}
		}
		writeResponse(httpServletResponse, serverApps.toString());
	}

	private void handleQueryLogcDeskInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Semaphore sp = new Semaphore(0, true);
		CenterActorManager.getDeskActor().put(() -> {
			Iterator<DeskInfo> it = DeskManager.getInst().getAllDesk().iterator();
			Map<String, Integer> mapDeskNum = new HashMap<>();
			int totalNum = 0;

			while (it.hasNext()) {
				DeskInfo desk = it.next();
				String address = desk.getBindServerSession().getRemoteAddress() + ":" + desk.getBindServerSession().getRemotePort();
				Integer num = mapDeskNum.get(address);
				if (num == null) {
					num = 0;
				}
				num++;
				totalNum++;
				mapDeskNum.put(address, num);
			}
			JSONObject deskNumList = new JSONObject();
			deskNumList.put("deskTotalNum", totalNum);
			JSONArray logicDeskNumList = new JSONArray();
			for (Entry<String, Integer> entry : mapDeskNum.entrySet()) {
				JSONObject logicDeskNum = new JSONObject();
				logicDeskNum.put("address", entry.getKey());
				logicDeskNum.put("num", entry.getValue());
				logicDeskNumList.add(logicDeskNum);
			}
			deskNumList.put("logicDeskNumList", logicDeskNumList);
			writeResponse(httpServletResponse, deskNumList.toString());
			sp.release();
			return null;
		});
		try {
			sp.tryAcquire(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			LogUtil.error(logger, e);
		}
	}

	private void handleQueryDesk(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Semaphore sp = new Semaphore(0, true);
		int deskId = Integer.parseInt(httpServletRequest.getParameter("deskId"));
		CenterActorManager.getDeskActor().put(() -> {
			JSONObject deskInfo = new JSONObject();
			Iterator<Player> it = null;
//			if(deskId == 0){//牛牛桌子
//				deskInfo.put("deskId", deskId);
//				deskInfo.put("gameId", GameType.NIUNIU.getValue());
//				deskInfo.put("roomId", RoomConst.NIUNIU);
//				it = LobbyGameManager.getInst().getRoomPlayerInfoList(GameType.NIUNIU.getValue()).iterator();
//			}else{
			DeskInfo desk = DeskManager.getInst().getDeskInfo(deskId);
			if (desk == null) {
				writeResponse(httpServletResponse, "400");
				sp.release();
				return null;
			}
			it = desk.getPlayerList().iterator();
			deskInfo.put("deskId", deskId);
			deskInfo.put("gameId", desk.getGameId());
			deskInfo.put("roomId", desk.getRoomId());
//			}

			JSONArray playerArr = new JSONArray();
			while (it.hasNext()) {
				Player player = it.next();
				JSONObject playerInfo = new JSONObject();
				playerInfo.put("playerId", player.getPlayerId());
				playerInfo.put("nick", player.getName());
				playerInfo.put("coin", player.getCoin());

				playerArr.add(playerInfo);
			}
			deskInfo.put("playerArr", playerArr);

			writeResponse(httpServletResponse, deskInfo.toString());
			sp.release();
			return null;
		});
		try {
			sp.tryAcquire(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			LogUtil.error(logger, e);
		}
	}

	private void handleQueryPlayerDesk(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		int playerId = Integer.parseInt(httpServletRequest.getParameter("playerId"));
		Semaphore sp = new Semaphore(0, true);
		CenterActorManager.getLogicActor(playerId).put(() -> {
			Player player = PlayerManager.getInstance().getPlayerById(playerId);
			if (player == null) {
				writeResponse(httpServletResponse, "400");
				sp.release();
				return null;
			}
			JSONObject playerInfo = new JSONObject();
			playerInfo.put("playerId", playerId);
			playerInfo.put("loginStatus", player.getLoginStatus());
			JSONObject roomInfo = new JSONObject();
			JSONObject server = new JSONObject();
//			if(player.getRoomId() == RoomConst.NIUNIU){
//				ServerSession serverSession = CenterServer.getInst().getNiuniuSession();
//				server.put("ip", serverSession.getRemoteAddress());
//				server.put("port", serverSession.getRemotePort());
//				roomInfo.put("deskId", 0);
//				roomInfo.put("gameId", GameType.NIUNIU.getValue());
//				roomInfo.put("roomId", RoomConst.NIUNIU);
//			} else 
			DeskInfo desk = player.getDeskInfo();
			if (desk != null) {
				ServerSession serverSession = desk.getBindServerSession();
				if (serverSession != null) {
					server.put("ip", serverSession.getRemoteAddress());
					server.put("port", serverSession.getRemotePort());
				} else {
					server.put("ip", "no create");
					server.put("port", "no create");
				}
				roomInfo.put("deskId", desk.getDeskId());
				roomInfo.put("gameId", desk.getGameId());
				roomInfo.put("roomId", desk.getRoomId());
			}
			if (roomInfo.has("deskId")) {
				playerInfo.put("room", roomInfo);
				playerInfo.put("server", server);
			}

			writeResponse(httpServletResponse, playerInfo.toString());
			sp.release();
			return null;
		});
		try {
			sp.tryAcquire(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			LogUtil.error(logger, e);
		}
	}

	private void handleRemoveDesk(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		int deskId = Integer.parseInt(httpServletRequest.getParameter("deskId"));
		CenterActorManager.getDeskActor().put(() -> {
			DeskInfo desk = DeskManager.getInst().getDeskInfo(deskId);
			if (desk != null) {
				desk.getPlayerList().forEach(e -> {
					LobbyGameManager.getInst().playerLeavePriRoom(GameType.getByValue(desk.getGameId()), e);
					LobbyGameManager.getInst().playerLeaveGameRoom(e);
					logger.info("离开了{}", e.getPlayerId());
					if (e.isLogout()) {
						e.logout();
					}
				});
				GroupDeskManager.getIns().removeDesk(desk);
				DeskManager.getInst().removeDesk(desk);
				if (desk.isCreated()) {
					ServerSession serverSession = desk.getBindServerSession();
					if (serverSession != null) {
//		            	serverSession.reduceFactor();
						serverSession.sendRequest(new CocoPacket(RequestCode.LOGIC_REMOVE_DESK, null, desk.getDeskId()));
					}
				}
				desk.getPlayerList().forEach(e -> e.write(new CocoPacket(RequestCode.GATE_KICK_PLAYER, null, e.getPlayerId())));
			}
			return null;
		});
		writeResponse(httpServletResponse, "200");
	}

	private void handleQueryAllGameInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		Semaphore sp = new Semaphore(0, true);
		CenterActorManager.getDeskActor().put(() -> {
			int num = PlayerManager.getInstance().getPlayerCount();

			JSONArray gamesArr = new JSONArray();
			JSONArray priGamesArr = new JSONArray();
			for (GameType type : GameType.values()) {
				//非私房
				JSONObject jo = new JSONObject();
				jo.put("gameId", type.getValue());
				JSONArray numObjArr = new JSONArray();
				Iterator<Pair<Integer, Integer>> it = LobbyGameManager.getInst().getRoomPlayerList(type.getValue()).iterator();
				while (it.hasNext()) {
					Pair<Integer, Integer> p = it.next();
					JSONObject numObj = new JSONObject();
					numObj.put("roomId", p.getLeft());
					numObj.put("num", p.getRight());
					numObjArr.add(numObj);
				}
				jo.put("playerNum", numObjArr);
				gamesArr.add(jo);
				//私房
				JSONObject priJo = new JSONObject();
				priJo.put("gameId", type.getValue());
				priJo.put("num", LobbyGameManager.getInst().getPriRoomPlayerNum(type));
				priGamesArr.add(priJo);
			}

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("onlineNum", num);
			jsonObject.put("allPlayerNum", PlayerManager.getInstance().getAllPlayers().size());
			jsonObject.put("game", gamesArr);
			jsonObject.put("priGame", priGamesArr);
			writeResponse(httpServletResponse, jsonObject.toString());
			sp.release();
			return null;
		});
		try {
			sp.tryAcquire(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			LogUtil.error(logger, e);
		}
	}

	private void handleConfPersonalRoom(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");
		ServerManager.getInst().getSessionList(AppId.LOGIC).forEach(e -> e.sendRequest(new CocoPacket(RequestCode.LOGIC_RELOAD_CONF_PERSONAL_ROOM, null)));
	}

	private void handleUpdateAgentBankCoin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		int id = Integer.parseInt(httpServletRequest.getParameter("id"));
		int agent_player_id = Integer.parseInt(httpServletRequest.getParameter("agent_player_id"));
		int back_player_id = Integer.parseInt(httpServletRequest.getParameter("back_player_id"));
		int amountCost = Integer.parseInt(httpServletRequest.getParameter("amount"));
		int type = Integer.parseInt(httpServletRequest.getParameter("type"));

		if (amountCost < 0) {
			writeResponse(httpServletResponse, "210");
			return;
		}

		CenterActorManager.getLogicActor(back_player_id).put(() -> {
//		if (back_player_id != 0) { // 如果該操作為充錯退款  先操作玩家能退回多少錢
			if (back_player_id == 0) {
				return amountCost;
			}
			Player player = PlayerManager.getInstance().getPlayerById(back_player_id);
			String ip = "";
			long pre_coin = 0;
			long pre_bank_coin = 0;
			int channel_id = 0;
			String package_id = "";
			String device = "";
			long coinCost = 0;
			long bankCost = amountCost;
			pre_bank_coin = player.getBankMoney();
			ip = player.getIp();
			pre_coin = player.getCoin();
			channel_id = player.getChannelId();
			package_id = String.valueOf(player.getPackageId());
			device = player.getDevice();

			if (player.getBankMoney() < amountCost) { // 銀行錢不夠扣
				bankCost = player.getBankMoney();
				coinCost = amountCost - player.getBankMoney();
				if (player.getCoin() < coinCost) { // 現金也不夠扣了
					coinCost = player.getCoin();
				}
			}
			if (bankCost > 0) {
				player.updateBankCoin(bankCost, false);
				player.addPay_money_agent(-bankCost); // 扣除充值额度
				player.write(ResponseCode.ACCOUNT_MODIFY_RANKCOIN
						, CommonCreator.createPBString(player.getBankMoney() + ""));
				logger.info("銀行更新 :玩家 {} 代理誤充扣回， 銀行金币減少{}，当前銀行总金币数为 {}", player.getPlayerId(), bankCost, player.getBankMoney());
				ServerManager.getInst().getMinLoadSession(AppId.LOG)
						.sendRequest(new CocoPacket(RequestCode.LOG_BANK
								, LogHelper.logBankSave_ex(player.getPlayerId(), BankAction.AGENT_WITHDRAW.getValue(), (int) bankCost, pre_coin, pre_bank_coin + bankCost, pre_bank_coin, ip, channel_id, package_id, device)));
			}
			if (coinCost > 0) {
				if (player.isGameing()) {
//						player.getDeskInfo().writeToLogic(new CocoPacket(RequestCode.LOGIC_UPDATE_MONEY
//								, CommonCreator.createPBInt32((int) player.getCoin()), player.getPlayerId()));
					player.getDeskInfo().writeToLogic(new CocoPacket(RequestCode.COUPLE_MONEY_CHANGE, CommonCreator.createPBPair((int) -coinCost, 1), player.getPlayerId()));
					logger.info("金币更新 :玩家 {} 代理誤充金幣退回，游戏中 预扣除 {}， Logic向Center反向扣除更新金币", player.getPlayerId(), coinCost);
				} else {
					player.updateCoin(coinCost, false);
					player.addPay_money_agent(-coinCost); // 扣除充值额度
					logger.info("金币更新 :玩家 {} 代理誤充金幣退回， 金币減少 {}，当前总金币数为 {}", player.getPlayerId(), coinCost, player.getCoin());
					player.write(ResponseCode.ACCOUNT_CHARGE_SUCC, CommonCreator.createPBInt32((int) -coinCost));
				}
				ServerManager.getInst().getMinLoadSession(AppId.LOG)
						.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
								, LogHelper.logLoseMoney(player.getPlayerId(), MoneySubAction.AGENT_WITHDRAW.getValue(), 0, (int) coinCost, pre_coin, pre_coin + coinCost, ip, channel_id, package_id, device, 0)));
			}
			PlayerSaver.savePlayerBase(player);
			int cost = (int) (coinCost + bankCost);
			String sql = "update agent_pay set amount = " + cost + ", player_out_last_bank_coin = " + player.getBankMoney() + " where id = " + id;
			CenterActorManager.getUpdateActor().put(() -> {
				ProcLogic.updateOfflineId(sql);
				return null;
			});
			if (cost > 0) {
				/// 扣款发送邮件
				String costStr = "";
				if (cost % 100 == 0) {
					costStr = String.valueOf(cost / 100);
				} else {
					costStr = String.valueOf((double) cost / 100);
				}
				MailEntity mail1 = MailEntity.createMail(back_player_id, player.getAvailableMailId(), 14, 0, costStr);
				CenterActorManager.getDbActor(back_player_id).put(() -> {
					MailEntity.insertMailIntoDataBase(mail1);
					return null;
				});
				player.addMail(mail1);
				player.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail1));
			}
			return cost;
		}, (e) -> {
			int amount = (int) e;
			Player player = PlayerManager.getInstance().getPlayerById(agent_player_id);
			String ip = "";
			long pre_coin = 0;
			long pre_bank_coin = 0;
			int channel_id = 0;
			String package_id = "";
			String device = "";
//				if (null != player) {
			pre_bank_coin = player.getBankMoney();
			ip = player.getIp();
			player.updateBankCoin(amount, true);
			PlayerSaver.savePlayer(player);
			logger.info("銀行更新 :玩家 {} 代理加錢，類型{}， 銀行金币增加{}，当前銀行总金币数为 {}", player.getPlayerId(), type, amount, player.getBankMoney());
			pre_coin = player.getCoin();
			channel_id = player.getChannelId();
			package_id = String.valueOf(player.getPackageId());
			device = player.getDevice();

			player.write(ResponseCode.ACCOUNT_MODIFY_RANKCOIN
					, CommonCreator.createPBString(player.getBankMoney() + ""));
			player.setShowBankMark(1);
			player.write(ResponseCode.ACCOUNT_SHOW_BANK_MARK, null); // 转账通知显示红点

			PlayerSaver.savePlayer(player);

			/// 收款发送邮件
			String costStr = "";
			if (amount % 100 == 0) {
				costStr = String.valueOf(amount / 100);
			} else {
				costStr = String.valueOf((double) amount / 100);
			}
			MailEntity mail1 = MailEntity.createMail(agent_player_id, player.getAvailableMailId(), 13, 0, costStr);
			CenterActorManager.getDbActor(agent_player_id).put(() -> {
				MailEntity.insertMailIntoDataBase(mail1);
				return null;
			});
			player.addMail(mail1);
			player.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail1));
//				} else {
//					Map<String, String> map_data = new HashMap<>();
//					map_data.put("bank_coin", String.valueOf(amount));
//					ASObject obj = PlayerSaver.offlineSavePlayerData(agent_player_id, map_data, "update player set bank_coin = bank_coin + " + amount + " where player_id = " + agent_player_id);
//					if (null == obj) {
//						ProcLogic.updateOfflineId("update agent_pay set status = 3 where id = " + id);
//						logger.debug("handleAgentPay id:" + id + "  agent_id:" + agent_player_id + "  amount:" + amount);
//						writeResponse(httpServletResponse, "201");
//						return;
//					}
//					pre_bank_coin = obj.getLong("bank_coin");
//					ip = obj.getString("ip");
//					pre_coin = obj.getLong("coin");
//					channel_id = obj.getInt("channel_id");
//					package_id = String.valueOf(obj.getInt("package_id"));
//					device = obj.getString("device");
//				}

			long tmp_data = pre_bank_coin + amount;
			String sql = "update agent_pay set player_in_last_bank_coin = " + tmp_data + ", status = 1  where id = " + id;
			CenterActorManager.getUpdateActor().put(() -> {
				ProcLogic.updateOfflineId(sql);
				return null;
			});
			int flag = type == BankAction.AGENT_TAX.getValue() ? type : BankAction.AGENT_SAVE.getValue();
//				int flag = 0 > amount ? BankAction.AGENT_WITHDRAW.getValue() : BankAction.AGENT_SAVE.getValue();
			ServerManager.getInst().getMinLoadSession(AppId.LOG)
					.sendRequest(new CocoPacket(RequestCode.LOG_BANK
							, LogHelper.logBankSave_ex(agent_player_id, flag, amount, pre_coin, pre_bank_coin + amount, pre_bank_coin, ip, channel_id, package_id, device)));
		}, CenterActorManager.getLogicActor(agent_player_id));
//		}
		writeResponse(httpServletResponse, "200");
	}

	private void handleWebRankPWD(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		int player_id = Integer.valueOf(httpServletRequest.getParameter("player_id"));
		String rank_pwd = httpServletRequest.getParameter("rank_pwd");

		Player player = PlayerManager.getInstance().getPlayerById(player_id);
//		if (null == player) {
//			Map<String, String> map_data = new HashMap<>();
//			map_data.put("bank_password", rank_pwd);
//			PlayerSaver.offlineSavePlayerData(player_id, map_data, "update player set bank_password = " + rank_pwd + " where player_id = " + player_id);
//		} else {
		player.setBankPassword(rank_pwd);
		PlayerSaver.savePlayer(player);
//		}
		writeResponse(httpServletResponse, "200");

	}

	/**
	 * 刷新公告配置与区域配置
	 *
	 * @param httpServletRequest
	 * @param httpServletResponse
	 */
	private void handleAnnouncement(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");
		AnnouncementProvider.getInst().reLoad();
		ProvinceProvider.getInst().reLoad();
		for (Player player : PlayerManager.getInstance().getOnlinePlayers()) {
			player.write(ResponseCode.ACCOUNT_DYNAMIC_CONFIG,
					CenterServer.getInst().createPBDynamicConfig(player.getChannelId(), player.getPackageId(), player));
		}
		noticeLoginReloadConf();
	}

	private void AgentHandReplyHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String id = httpServletRequest.getParameter("id");
		int player_id = Integer.valueOf(httpServletRequest.getParameter("player_id"));
		String content = httpServletRequest.getParameter("content");
		int agent_id = Integer.valueOf(httpServletRequest.getParameter("agent_id"));

		Player player = PlayerManager.getInstance().getPlayerById(player_id);
		int mail_id = 0;
//		if (null == player) {
//			mail_id = RankManager.getInst().geneOfflineMailId(player_id);
//		} else {
		mail_id = player.getAvailableMailId();
//		}
		MailEntity mail = MailEntity.createMail_ex(player_id, mail_id, 10, 0, agent_id, "", content);
		CenterActorManager.getDbActor(player_id).put(() -> {
			MailEntity.insertMailIntoDataBase(mail);
			return null;
		});
//		if (null != player) {
		player.addMail(mail);
		player.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail));
//		}

		/// 修改回复对应的文件
		Map<String, Object> map_data = new HashMap<>();
		map_data.put("mail_id", mail_id);
		Map<String, Object> map_where = new HashMap<>();
		map_where.put("id", id);
		try {
			DBUtil.executeUpdate("agent_message_info", map_where, map_data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		writeResponse(httpServletResponse, "200");
	}

	private void CGConfigHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");
		PlayerCGConfigProvider.getInst().reLoad();
	}

	private void AgentPlanUpdateHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");
		String str_req = httpServletRequest.getParameter("data");
		System.out.println("str_req:" + str_req);

		if (0 >= str_req.length()) {
			return;
		}
		Gson gson = new Gson();
		PlayerAgentRequest gaon_data = gson.fromJson(str_req, PlayerAgentRequest.class);
		CenterActorManager.getUpdateActor().put(() -> {
			for (ASObject obj : gaon_data.getList()) {
				int player_id = obj.getInt("player_id");
				Player player = PlayerManager.getInstance().getPlayerById(player_id);
//				if (null != player) {
				player.setAgent_plan(obj.getInt("agent_plan"));
				PlayerSaver.savePlayer(player);
//				} else {
//					Object data = DataManager.getInst().getCache().query(player_id);
//					if (data == null) {
//					} else {
//						//如果在缓存就要修改缓存数据了
//						CharData charData = (CharData) data;
//						ASObject tmp_obj = charData.getModuleData(DBAction.PLAYER);
//						Object[] objArray = (Object[]) tmp_obj.get("" + player_id);
//						ASObject baseData = (ASObject) objArray[0];
//						baseData.put("agent_plan", tmp_obj.getInt("agent_plan"));
//						//修改这个然后存盘了啊
//						DataManager.getInst().saveModule(player_id, DBAction.PLAYER, tmp_obj);
//					}
//				}
			}
			return null;
		});
	}

	private void AgentAuickReplyHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		AgentAuickReplyProvider.getInst().reLoad();
	}

	private void handleExchangeBigLimit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		ExchangeBigLimitProvider.getInst().reLoad();
	}

	private void handleExchangeRestore(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		int player_id = Integer.parseInt(httpServletRequest.getParameter("player_id"));
		int amount = Integer.parseInt(httpServletRequest.getParameter("amount")) / 100;
		int agentId = Integer.parseInt(httpServletRequest.getParameter("agentId"));
		boolean errorAlipay = Integer.parseInt(httpServletRequest.getParameter("errorAlipay")) == 1;
		long pre_coin = 0;
		String ip = "";
		int channel_id = 0;
		String package_id = "";
		String device = "";
		Player player = PlayerManager.getInstance().getPlayerById(player_id);
//		if (null != player) {
		pre_coin = player.getCoin();
		ip = player.getIp();
		channel_id = player.getChannelId();
		package_id = String.valueOf(player.getPackageId());
		device = player.getDevice();
//			player.updateCoin(amount, true);
//			PlayerSaver.savePlayerBase(player);
//			logger.info("金币更新 :玩家 {} 兑换失败退钱， 金币增加 {}，当前总金币数为 {}", player.getPlayerId(), amount, player.getCoin());
//			player.write(ResponseCode.ACCOUNT_EXCHANGE_SUCC, AccountCreator.createPBLoginSucc(player));
		// 退錢改為郵件通知
		MailEntity mail = MailEntity.createMail(player.getPlayerId(), player.getAvailableMailId(), 12, amount, String.valueOf(agentId), String.valueOf(amount));
		player.addMail(mail);
		CenterActorManager.getDbActor(player.getPlayerId()).put(() -> {
			MailEntity.insertMailIntoDataBase(mail);
			return null;
		});
//		} else {
//			Map<String, String> map_data = new HashMap<>();
//			map_data.put("coin", String.valueOf(amount));
//			ASObject obj = PlayerSaver.offlineSavePlayerData(player_id, map_data, "update player set coin = coin + " + amount + " where player_id = " + player_id);
//			if (null == obj) {
//				logger.error("handleExchangeRestore player_id:" + player_id);
//				return;
//			}
//			pre_coin = obj.getLong("coin");
//			ip = obj.getString("ip");
//			channel_id = obj.getInt("channel_id");
//			package_id = String.valueOf(obj.getInt("package_id"));
//			device = obj.getString("device");
//		}
		ServerManager.getInst().getMinLoadSession(AppId.LOG)
				.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
						, LogHelper.logMoney(player_id, MoneyAction.GAIN.getValue(), MoneySubAction.EXCHANGE_RESTORE.getValue(), 0, amount, pre_coin, pre_coin + amount, ip, channel_id, package_id, device, 0)));
		player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.EXCHANGE_RESTORE_FAIL));
		if (errorAlipay) { // 解绑支付宝
			player.setAlipayAccount("");
			player.setAlipayName("");
			PlayerSaver.savePlayerAlipay(player);
			player.write(ResponseCode.ACCOUNT_BIND_ALI_PAY, AccountCreator.createPBLoginSucc(player));
		}
	}


	private void PlayerOnlyShowAgentHandler(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String str_req = httpServletRequest.getParameter("data");
		System.out.println("str_req:" + str_req);

		if (0 >= str_req.length()) {
			return;
		}
		Gson gson = new Gson();
		PlayerAgentRequest gaon_data = gson.fromJson(str_req, PlayerAgentRequest.class);
		CenterActorManager.getUpdateActor().put(() -> {
			for (ASObject obj : gaon_data.getList()) {
				int player_id = obj.getInt("player_id");
				Player player = PlayerManager.getInstance().getPlayerById(player_id);
//				if (null != player) {
				player.setOnly_show_agent(obj.getBoolean("only_show_agent"));
				PlayerSaver.savePlayer(player);
//				} else {
//					Object data = DataManager.getInst().getCache().query(player_id);
//					if (data == null) {
//						PlayerSaver.saveOfflinePlayer("update player set only_show_agent = " + obj.getBoolean("only_show_agent") + " where player_id = " + obj.getInt("player_id"));
//					} else {
//						//如果在缓存就要修改缓存数据了
//						CharData charData = (CharData) data;
//						ASObject tmp_obj = charData.getModuleData(DBAction.PLAYER);
//						Object[] objArray = (Object[]) tmp_obj.get("" + player_id);
//						ASObject baseData = (ASObject) objArray[0];
//						baseData.put("only_show_agent", tmp_obj.getBoolean("only_show_agent"));
//						//修改这个然后存盘了啊
//						DataManager.getInst().saveModule(player_id, DBAction.PLAYER, tmp_obj);
//					}
//				}
			}
			return null;
		});

		writeResponse(httpServletResponse, "200");
	}

	private void handleTest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		int player_id = Integer.parseInt(httpServletRequest.getParameter("player_id"));
		int amount = Integer.parseInt(httpServletRequest.getParameter("amount"));
		Player player = PlayerManager.getInstance().getPlayerById(player_id);
		if (null != player) {
			player.getDeskInfo().writeToLogic(new CocoPacket(RequestCode.COUPLE_MONEY_CHANGE, CommonCreator.createPBPair(amount, 1), player.getPlayerId()));
		}
	}

	private void handleReturnOrderidPay(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String url = httpServletRequest.getParameter("url");
		int player_id = Integer.parseInt(httpServletRequest.getParameter("player_id"));
		int code = Integer.parseInt(httpServletRequest.getParameter("code"));

		Player player = PlayerManager.getInstance().getPlayerById(player_id);
		if (null != player) {

			int channelId = player.getChannelId();
			int packageId = player.getPackageId();
			ChannelConfig channel_conf = ChannelInfoProvider.getInst().getChannelConfig(channelId, packageId, player.isReview());

			if (null != channel_conf) {
				if (1 != code) {
					player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(code));
				} else {
					player.write(ResponseCode.ACCOUNT_GENE_ORDER, CommonCreator.createPBPairString(channel_conf.getPay_method(), url));
				}
			} else {
				logger.debug("channelId:" + channelId + "   packageId:" + packageId);
			}
		}

		writeResponse(httpServletResponse, "200");
	}

	private void handleAgentPay(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		int id = Integer.parseInt(httpServletRequest.getParameter("id"));
		int player_id = Integer.parseInt(httpServletRequest.getParameter("player_id"));
		int agent_id = Integer.parseInt(httpServletRequest.getParameter("agent_id"));
		int amount = Integer.parseInt(httpServletRequest.getParameter("amount"));

		if (amount < 0) {
			writeResponse(httpServletResponse, "201");
			return;
		}

		ProcLogic.agentTransfer(id, agent_id, player_id, amount, 0, 1, (e) -> {
			int ret = (int) e;
			Player player_in = PlayerManager.getInstance().getPlayerById(player_id);
			Player player_out = PlayerManager.getInstance().getPlayerById(agent_id);
			if (200 == ret) {
				/// 购入者发送邮件
				MailEntity mail1 = MailEntity.createMail(player_id, player_in.getAvailableMailId(), 7, 0,
						player_out.getName(), String.valueOf(agent_id), String.valueOf(amount / 100));
				CenterActorManager.getDbActor(player_id).put(() -> {
					MailEntity.insertMailIntoDataBase(mail1);
					return null;
				});
				// 购入发邮件
				player_in.addMail(mail1);
				player_in.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail1));

				// 转出购入加明细
				TransferData transferData = TransferData.createTransferData(id, agent_id, player_id,
						player_in.getName(), amount, 1, MiscUtil.getCurrentSeconds());
				player_in.addTransferList(transferData);
				player_out.addTransferList(transferData);
			}
			writeResponse(httpServletResponse, "" + e);
		});

	}

	private void handleAgentInfoDb(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String agent_mail_content = httpServletRequest.getParameter("agent_mail_content");
		String agent_mail_tatil = httpServletRequest.getParameter("agent_mail_tatil");
		String player_id = httpServletRequest.getParameter("player_id");
		handleMainSendMail(Integer.valueOf(player_id), agent_mail_content, agent_mail_tatil, 9);
	}

	private void handleAgentInfo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");
		int plan_id = Integer.parseInt(httpServletRequest.getParameter("plan_id"));
		int agent_id = Integer.parseInt(httpServletRequest.getParameter("agent_id"));
		String platform_id = httpServletRequest.getParameter("platform_id");

		if (0 == agent_id) {
			AgentInfoProvider.getInst().reLoad();
		} else {
			AgentInfoProvider.getInst().delAgentInfo(plan_id, platform_id, agent_id);
		}
	}

	private void handleMainSend(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String player_ids = httpServletRequest.getParameter("player_ids");
		String title = httpServletRequest.getParameter("title");
		String ret_content = httpServletRequest.getParameter("content");
		if (0 >= ret_content.length()) {
			return;
		}

		for (String param : player_ids.split(",")) {
			int player_id = Integer.valueOf(param);
			handleMainSendMail(player_id, ret_content, title, 4);
		}

		writeResponse(httpServletResponse, "success");
	}

	public static void handleMainSendMail(int player_id, String content, String title, int contenti_id) {
		Player player = PlayerManager.getInstance().getPlayerById(player_id);
		int mail_id = 0;
//		if (null == player) {
//			mail_id = RankManager.getInst().geneOfflineMailId(player_id);
//		} else {
		mail_id = player.getAvailableMailId();
//		}
		MailEntity mail = MailEntity.createMail_ex(player_id, mail_id, contenti_id, 0, 0, title, content);
		CenterActorManager.getDbActor(player_id).put(() -> {
			MailEntity.insertMailIntoDataBase(mail);
			return null;
		});
//		if (null != player) {
		player.addMail(mail);
		player.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail));
//		}
	}

	private void handlePlayerConfException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		writeResponse(httpServletResponse, "200");
		ConfPlayerExceptionProvider.getInst().reLoad();
	}

	private void handleUpdatePayParam(HttpServletRequest request, HttpServletResponse response) {
		String orderId = request.getParameter("order_id");
		int payType = Integer.parseInt(request.getParameter("pay_type"));
		int payChannel = Integer.parseInt(request.getParameter("pay_channel"));

		Map<String, Object> where = new HashMap<>();
		where.put("order_id", orderId);
		List<ASObject> list = DataQueryResult.load("order_info", where);
		if (list.size() != 1) {
			writeResponse(response, "order_not_exist");
			return;
		}
		ASObject data = list.get(0);
		if (0 == data.getInt("status") || 1 == data.getInt("status")) {
			Map<String, Object> updateData = new HashMap<>();
			updateData.put("pay_type", payType);
			updateData.put("pay_channel", payChannel);
			try {
				DBUtil.executeUpdate("order_info", where, updateData);
			} catch (SQLException e) {
				e.printStackTrace();
				writeResponse(response, "exception");
				return;
			}
			writeResponse(response, "success");
		} else {
			writeResponse(response, "status:" + data.getInt("status"));
		}
	}

	private void handleWebCoin(HttpServletRequest request, HttpServletResponse response) {
		int playerId = Integer.parseInt(request.getParameter("player_id"));
		long coin = Integer.parseInt(request.getParameter("coin")) * 100;

		Player player = PlayerManager.getInstance().getPlayerById(playerId);
		long pre_coin = 0;
		String ip = "";
		int channel_id = 0;
		String package_id = "";
		String device = "";
//		if (player == null) {
//
//			Map<String, String> map_data = new HashMap<>();
//			map_data.put("coin", String.valueOf(coin));
//			ASObject obj = PlayerSaver.offlineSavePlayerData(playerId, map_data, "update player set coin = coin + " + coin + " where player_id = " + playerId);
//			if (null == obj) {
//				logger.error("handleWebCoin player_id:" + playerId);
//				writeResponse(response, "201");
//				return;
//			}
//
//			pre_coin = obj.getLong("coin");
//			ip = obj.getString("ip");
//			channel_id = obj.getInt("channel_id");
//			package_id = String.valueOf(obj.getInt("package_id"));
//			device = obj.getString("device");
//		} else {
		pre_coin = player.getCoin();
		ip = player.getIp();
		channel_id = player.getChannelId();
		package_id = "" + player.getPackageId();
		device = "" + player.getDevice();

		CenterActorManager.getLogicActor(player.getPlayerId()).put(() -> {
			if (player.isGameing()) {
				player.getDeskInfo().writeToLogic(new CocoPacket(RequestCode.COUPLE_MONEY_CHANGE, CommonCreator.createPBPair((int) coin, 1), player.getPlayerId()));
				logger.info("金币更新 :玩家 {} 后台充值，游戏中 预增加 {}， Logic向Center反向增加更新金币", player.getPlayerId(), coin);
			} else {
				player.updateCoin(coin, true);
				PlayerSaver.savePlayerBase(player);
				logger.info("金币更新 :玩家 {} 后台充值， 金币增加 {}，当前总金币数为 {}", player.getPlayerId(), coin, player.getCoin());
			}
//				player.updateCoin(coin, true);
//				logger.info("金币更新 :玩家 {} 后台充值， 金币增加 {}，当前总金币数为 {}", player.getPlayerId(), coin, player.getCoin());
			player.write(ResponseCode.ACCOUNT_UPDATE_DATA, AccountCreator.createPBLoginSucc(player));
			return null;
		});
//		}

		int flag = coin > 0 ? MoneyAction.GAIN.getValue() : MoneyAction.LOSE.getValue();
		ServerManager.getInst().getMinLoadSession(AppId.LOG)
				.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
						, LogHelper.logMoney(playerId, flag, MoneySubAction.WEB_UPDATE.getValue(), 0, coin, pre_coin, pre_coin + coin, ip, channel_id, package_id, device, 0)));

		writeResponse(response, "200");
	}

//	public void updateOfflinePlayerCoin(int player_id, long coin) {
//		Connection conn = null;
//		PreparedStatement stat = null;
//		ResultSet rs = null;
//		long tmpCoin = 0;
//		try {
//			conn = DBManager.getConnection();
//			stat = conn.prepareStatement("select coin from player where player_id = " + player_id);
//			rs = stat.executeQuery();
//			if (rs.next()) {
//				tmpCoin = rs.getInt("coin") + coin * 100;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return;
//		} finally {
//			DBManager.close(conn, stat, rs);
//		}
//
//		if (0 < tmpCoin) {
//			updatePlayerCoin(player_id, tmpCoin);
//		}
//	}

//	public static void updateOfflinePlayerData(String sql) {
//		Connection conn = null;
//		PreparedStatement stat = null;
//		ResultSet rs = null;
//		try {
//			conn = DBManager.getConnection();
//			stat = conn.prepareStatement(sql);
//			stat.executeUpdate();
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return;
//		} finally {
//			DBManager.close(conn, stat, rs);
//		}
//	}

//	public void updatePlayerCoin(int player_id, long coin) {
//		Map<String, Object> updateData = new HashMap<>();
//		updateData.put("coin", coin);
//		Map<String, Object> where = new HashMap<>();
//		where.put("player_id", player_id);
//		try {
//			DBUtil.executeUpdate("player", where, updateData);
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return;
//		}
//	}

	private void handleBlockPlayer(HttpServletRequest request, HttpServletResponse response) {
		String playerIds = request.getParameter("player_id");
		List<String> params = new ArrayList<>();

		for (String param : playerIds.split(",")) {
			params.add(param);
		}

		for (String param : params) {
			int playerId = Integer.parseInt(param);
			Player player = PlayerManager.getInstance().getPlayerById(playerId);
			if (null != player) {
				player.write(ResponseCode.KICK_PLAYER, null);

				CenterModule.getIns().playerLogout(player.getSession(), player);
			}
		}
	}

	private void handleUnbindAlipay(HttpServletRequest request, HttpServletResponse response) {
	}

	private void handleConfigChannelSwitch(HttpServletRequest request, HttpServletResponse response) {
		String id = request.getParameter("id");
		String packId = request.getParameter("package_id");
		int channelId = 0;
		int packageId = 0;
		try {
			channelId = Integer.parseInt(id);
			packageId = Integer.parseInt(packId);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}
		ChannelInfoProvider.getInst().reLoad();
		for (Player player : PlayerManager.getInstance().getOnlinePlayers()) {
			if (player.getGameChannel().equals(id) && player.getPackageId() == packageId) {
				player.write(ResponseCode.ACCOUNT_DYNAMIC_CONFIG, CenterServer.getInst().createPBDynamicConfig(channelId, packageId, player));
			}
		}
		noticeLoginReloadConf();
	}

	private void handlePaomadeng(HttpServletRequest request, HttpServletResponse response) {

		String player_id = request.getParameter("player_id");
		String timeFrom = request.getParameter("timeFrom");
		String timeTo = request.getParameter("timeTo");
		String content = request.getParameter("content");
		int type = Integer.valueOf(request.getParameter("type"));        /// 0.正常 1.插入 2.修改 3.删除
		String id = request.getParameter("id");
		String delay = request.getParameter("delay");
		int time_from = MiscUtil.getSecondsOfTimeStamp_ex(timeFrom, "yyyy-MM-dd hh:mm");
		int time_to = MiscUtil.getSecondsOfTimeStamp_ex(timeTo, "yyyy-MM-dd hh:mm");

		List<ASObject> list_object = new ArrayList<>();
		ASObject object = new ASObject();
		object.put("timeFrom", time_from);
		object.put("timeTo", time_to);
		object.put("content", content);
		object.put("id", id);
		object.put("delay", delay);
		list_object.add(object);

		if (null != player_id && 0 != Integer.valueOf(player_id)) {
			Player player = PlayerManager.getInstance().getPlayerById(Integer.valueOf(player_id));
			if (null != player) {
				player.write(ResponseCode.LOBBY_PAO_MA_DENG, LobbyCreator.createPBPaomadengList(list_object, type));
			}
		} else {
			List<ServerSession> sessionList = ServerManager.getInst().getSessionList(AppId.GATE);
			if (sessionList == null) {
				return;
			}
			sessionList.forEach(e -> e.sendRequest(new CocoPacket(RequestCode.GATE_BROAD_CAST_MESSAGE
					, CommonCreator.createPBGameProtocol(ResponseCode.LOBBY_PAO_MA_DENG.getValue()
					, LobbyCreator.createPBPaomadengList(list_object, type).toByteArray()))));
		}

		PaoMaDengProvider.getInst().reLoad();

		writeResponse(response, "success");
	}

	private void handleDynamicChange(HttpServletRequest request, HttpServletResponse response) {
		DynamicInfoProvider.getInst().reLoad();
		ServerManager.getInst().getSessionList(AppId.LOGIC).forEach(e -> e.sendRequest(new CocoPacket(RequestCode.LOGIC_RELOAD_DYNAMIC, null)));
		ServerManager.getInst().getSessionList(AppId.LOGIC).forEach(e -> e.sendRequest(new CocoPacket(RequestCode.LOGIC_RELOAD_CONF_ROOM, null)));
		for (Player player : PlayerManager.getInstance().getOnlinePlayers()) {
			player.write(ResponseCode.ACCOUNT_DYNAMIC_CONFIG, CenterServer.getInst().createPBDynamicConfig(player.getChannelId(), player.getPackageId(), player));
		}
	}

	private void handleExchangeCallback(HttpServletRequest request, HttpServletResponse response) {
		String order_id = request.getParameter("order_id");
		String player_id = request.getParameter("player_id");
		String need_pay = request.getParameter("need_pay");
		Player player = PlayerManager.getInstance().getPlayerById(Integer.valueOf(player_id));
		long last_coin = 0;
//		if (null == player) {
//			ASObject obj = PlayerLoader.getOfflinePlayerData(Integer.valueOf(player_id));
//			if (null == obj) {
//				logger.error("handleExchangeCallback order_id:" + order_id + "   player_id:" + player_id);
//				return;
//			}
//			last_coin = obj.getLong("coin");
//		} else {
		last_coin = player.getCoin();
		writeResponse(response, "success");
//		}

		player.addExchange_total(Long.valueOf(need_pay));

		Map<String, Object> where = new HashMap<>();
		where.put("order_id", order_id);
		Map<String, Object> data = new HashMap<>();
		data.put("last_coin", last_coin);
		try {
			DBUtil.executeUpdate("exchange", where, data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	private void handleRecharge(HttpServletRequest request, HttpServletResponse response) {

	}


	private void handleMsgFeedBack(HttpServletRequest request, HttpServletResponse response) {
		int playerId = Integer.parseInt(request.getParameter("player_id"));
		String content = request.getParameter("content");
		int time = Integer.parseInt(request.getParameter("contact_time"));
		Player player = PlayerManager.getInstance().getPlayerById(playerId);
		if (player == null) {
			return;
		}
		player.write(ResponseCode.MAIL_KEFU_RESPONSE, CommonCreator.createPBStringList(String.valueOf(time), content));
	}


	private void handleConfRoom(HttpServletRequest request, HttpServletResponse response) {
		CoupleRoomInfoProvider.getInst().reLoad();
		RankInfoProvider.getInst().reLoad();
		ConfNiuProvider.getInst().reLoad();
		CenterActorManager.getDeskActor().put(() -> {
			LobbyGameManager.getInst().addRoom();
			return null;
		});
		List<ServerSession> sessionList = ServerManager.getInst().getSessionList(AppId.GATE);
		if (sessionList == null) {
			return;
		}
		ServerManager.getInst().getSessionList(AppId.LOGIC).forEach(e -> e.sendRequest(new CocoPacket(RequestCode.LOGIC_RELOAD_CONF_ROOM, null)));
		sessionList.forEach(e -> e.sendRequest(new CocoPacket(RequestCode.GATE_BROAD_CAST_MESSAGE
				, CommonCreator.createPBGameProtocol(ResponseCode.ACCOUNT_MODIFY_ROOM_CONF.getValue()
				, CommonCreator.createPBString(CoupleRoomInfoProvider.getInst().getConfString()).toByteArray()))));
	}


	private void handleRankReward(HttpServletRequest request, HttpServletResponse response) {
		RankInfoProvider.getInst().reLoad();
		List<ServerSession> sessionList = ServerManager.getInst().getSessionList(AppId.GATE);
		if (sessionList == null) {
			return;
		}
		sessionList.forEach(e -> e.sendRequest(new CocoPacket(RequestCode.GATE_BROAD_CAST_MESSAGE
				, CommonCreator.createPBGameProtocol(ResponseCode.ACCOUNT_MODIFY_RANK_CONFIG.getValue()
				, CommonCreator.createPBString(RankInfoProvider.getInst().getConfString()).toByteArray()))));
	}

	private void writeResponse(HttpServletResponse response, String result) {
		response.setStatus(HttpStatus.OK_200);
		try {
			response.getWriter().write(result);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
