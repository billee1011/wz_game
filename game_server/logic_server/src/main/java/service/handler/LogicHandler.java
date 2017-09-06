package service.handler;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;

import actor.LogicActorManager;
import config.CoupleRoomInfoProvider;
import config.DynamicInfoProvider;
import config.bean.CoupleRoom;
import config.provider.ConfNiuProvider;
import config.provider.PersonalConfRoomProvider;
import define.AppId;
import define.DealZimo;
import io.netty.channel.ChannelHandlerContext;
import logic.AbstractDesk;
import logic.Desk;
import logic.DeskMgr;
import logic.debug.ArrayPai;
import logic.debug.CheckFanType;
import logic.define.BonusType;
import logic.define.GameType;
import logic.majiong.CoupleMJDesk;
import logic.majiong.MJDesk;
import logic.majiong.PlayerInfo;
import logic.majiong.SCMJDesk;
import logic.majiong.XueNiuDesk;
import logic.majiong.XuezhanDesk;
import logic.majiong.define.Gender;
import logic.poker.PokerDesk;
import logic.poker.cpddz.CoupleDdzDesk;
import logic.poker.ddz.DdzDesk;
import logic.poker.lzddz.LzDdzDesk;
import logic.poker.niuniu.NiuNiuDesk;
import logic.poker.niuniu.zhuang.ClassZhuangNiuDesk;
import logic.poker.niuniu.zhuang.GrabZhuangNiuDesk;
import logic.poker.niuniu.zhuang.ZhuangNiuDesk;
import logic.poker.zjh.ZjhDesk;
import network.AbstractHandlers;
import network.NetClient;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.Lobby;
import protobuf.creator.CommonCreator;
import protobuf.creator.CoupleCreator;
import protocol.c2s.RequestCode;
import service.LogicApp;
import util.LogUtil;
import util.Pair;

/**
 * Created by Administrator on 2017/2/7.
 */
public class LogicHandler extends AbstractHandlers {
	private static Logger logger = LoggerFactory.getLogger(LogicHandler.class);

	@Override
	protected void registerAction() {
		registerAction(RequestCode.LOGIC_CREATE_DESK.getValue(), this::actionCreateDesk, CoupleMajiang.PBCreateDesk.getDefaultInstance());
		registerAction(RequestCode.LOGIC_ENTER_NIUNIU_DESK.getValue(), this::actionEnterNiuniuDesk, CoupleMajiang.PBCreateDesk.getDefaultInstance());
		registerAction(RequestCode.COUPLE_READY.getValue(), this::actionReady);
		registerAction(RequestCode.COUPLE_MJ_DISCARD_CARD.getValue(), this::actionDiscardCard, CoupleMajiang.PBDiscardCardReq.getDefaultInstance());
		registerAction(RequestCode.COUPLE_MJ_CHI_CARD.getValue(), this::actionChiCard, CoupleMajiang.PBChiReq.getDefaultInstance());
		registerAction(RequestCode.COUPLE_MJ_KE_CARD.getValue(), this::actionKeCard, CoupleMajiang.PBKeReq.getDefaultInstance());
		registerAction(RequestCode.COUPLE_MJ_GANG_CARD.getValue(), this::actionGangCard, CoupleMajiang.PBGangReq.getDefaultInstance());
		registerAction(RequestCode.COUPLE_MJ_TING_CARD.getValue(), this::actionTingCard, CoupleMajiang.PBTingReq.getDefaultInstance());
		registerAction(RequestCode.COUPLE_MJ_HU_CARD.getValue(), this::actionHuCard, CoupleMajiang.PBHuReq.getDefaultInstance());
		registerAction(RequestCode.COUPLE_GUO.getValue(), this::actionGuo);
		registerAction(RequestCode.COUPLE_DO_TING.getValue(), this::actionDoTing, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.COUPLE_RESET_GAME.getValue(), this::actionResetGame);
		registerAction(RequestCode.XUENIU_CHOSE_CARDS_SWITCH.getValue(), this::actionChoseSwitchCards, Common.PBInt32List.getDefaultInstance());
		registerAction(RequestCode.XUENIU_CHOSE_TYPE.getValue(), this::actionChoseType, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.NIUNIU_LOWER_CHIP.getValue(), this::actionNiuNiuChip, Common.PBPair.getDefaultInstance());
		registerAction(RequestCode.NIUNIU_ROB_BANNER.getValue(), this::actionRobBanner);
		registerAction(RequestCode.NIUNIU_GIVE_UP_BANNER.getValue(), this::actionGiveUpBanner);
		registerAction(RequestCode.NIUNIU_CANCEL_BANNER.getValue(), this::actionCancelBanner);
		registerAction(RequestCode.NIUNIU_GET_HIS_RECORD.getValue(), this::actionGetHisRecord);
		registerAction(RequestCode.NIUNIU_RESET_NIUNIU.getValue(), this::actionResetNiuniu);
		registerAction(RequestCode.NIUNIU_RENEWED.getValue(), this::actionNiuNiuRenewed, Common.PBPairList.getDefaultInstance());
		registerAction(RequestCode.LOGIC_PLAYER_LEAVE_DESK.getValue(), this::actionLeaveDesk);
		registerAction(RequestCode.LOBBY_PLAYER_LOGOUT.getValue(), this::actionPlayerLogout);
		registerAction(RequestCode.LOGIC_CREATE_ROOM_DESK.getValue(), this::actionCreateRoomDesk, CoupleMajiang.PBLoigcCreareRoomReq.getDefaultInstance());
		registerAction(RequestCode.LOGIC_ROOM_GAME_START.getValue(), this::actioRoomGameStart, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.LOGIC_DISBAND_ROOM_DESK.getValue(), this::actionDisbandRoomDesk, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.LOGIC_GIVE_UP.getValue(), this::actionAdmitDefeat);
		registerAction(RequestCode.LOGIC_UPDATE_MONEY.getValue(), this::actionUpdateMoney, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.LOGIC_CONTINUE_GAME.getValue(), this::actionContinueGame);
		registerAction(RequestCode.NIUNIU_GET_RANK_LIST.getValue(), this::actionGetNiuniuRankList);
		registerAction(RequestCode.LOGIC_RELOAD_DYNAMIC.getValue(), this::actionReloadDynamicProperties);
		registerAction(RequestCode.LOGIC_RELOAD_CONF_ROOM.getValue(), this::actionReloadConfRoom);
		registerAction(RequestCode.LOGIC_RELOAD_CONF_PERSONAL_ROOM.getValue(), this::actionReloadConfPersonalRoom);
		registerAction(RequestCode.COUPLE_MONEY_CHANGE.getValue(), this::actionPlayerMoneyChange, Common.PBPair.getDefaultInstance());

		registerAction(RequestCode.DDZ_ROB_LORD.getValue(), this::actionRobLord, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.DDZ_JIABEI.getValue(), this::actionJiabei, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.DDZ_DISCARD_CARD.getValue(), this::actionDdzDiscardCard, Common.PBInt32List.getDefaultInstance());
		registerAction(RequestCode.DDZ_PASS.getValue(), this::actionDdzPass);
		registerAction(RequestCode.DDZ_TUOGUAN.getValue(), this::actionDdzTouguan);
		registerAction(RequestCode.DDZ_CANCEL_TUOGUAN.getValue(), this::actionDdzCancelTuoguan);
//		registerAction(RequestCode.LOBBY_PLAYER_RELOGIN.getValue(), this::actionPlayerReLogin);


		registerAction(RequestCode.ZJH_ADD_GOLD.getValue(), this::actionZjhAddGold, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.ZJH_LOOK_CARD.getValue(), this::actionZjhLookCard);
		registerAction(RequestCode.ZJH_COMPARE_CARD.getValue(), this::actionZjhCompareCard, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.ZJH_GIVE_UP.getValue(), this::actionZjhGiveUp);
		registerAction(RequestCode.ZJH_SHOW_CARDS.getValue(), this::actionShowCards);
//		registerAction(RequestCode.LOGIC_ENTER_ZJH_DESK.getValue(), this::actionEnterZjhDesk, Common.PBPlayerInfo.getDefaultInstance());
//        registerAction(RequestCode.ZJH_ALL_GOLD_IN.getValue(), this::actionALLGoldIn);
		registerAction(RequestCode.ZJH__FULL_PRESSURE.getValue(), this::actionFullPressure);
//		registerAction(RequestCode.LOGIN_DISBANDE_DESK.getValue(), this::actionDisbandeDesk);

		registerAction(RequestCode.LOGIC_ENTER_GRAB_NIU_DESK.getValue(), this::actionEnterGrabDesk, Common.PBPlayerInfo.getDefaultInstance());
		registerAction(RequestCode.GRAB_NIU_GRAB_ZHUANG.getValue(), this::actionGrabNiuGrab, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.GRAB_NIU_ADD_BET.getValue(), this::actionGrabNiuAddBet, Common.PBInt32.getDefaultInstance());
		registerAction(RequestCode.GRAB_NIU_PLAYER_CAL.getValue(), this::actionGrabNiuPlayerCal);
		
		registerAction(RequestCode.LOGIC_DEBUG_ARRAY_PAI.getValue(), this::clientArrayPai, Common.PBIntString.getDefaultInstance());
		registerAction(RequestCode.LOGIC_DEBUG_CHECK_FAN_TYPE.getValue(), this::checkMjFanType, Common.PBIntListList.getDefaultInstance());
		
		registerAction(RequestCode.LOGIC_REMOVE_DESK.getValue(), this::removeDesk);
		registerAction(RequestCode.LOGIC_REMOVE_SERVER.getValue(), this::removeServer, Common.PBInt32.getDefaultInstance());
	}
	
	/** 后台关闭服务器 */
	private void removeServer(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		if(packet.getPlayerId() != 0){
			return;
		}
		Common.PBInt32 endTime = message.get();
		LogicApp.getInst().beginStop(endTime.getValue());
	}
	
	private void removeDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		LogicActorManager.getDeskActor(packet.getPlayerId()).put(() -> {
			Desk desk = DeskMgr.getInst().getDeskByDeskId(packet.getPlayerId());
			if(desk == null){
				return null;
			}
			DeskMgr.getInst().removeDeskByGm(desk);
			return null;
		});
	}
	
	private void checkMjFanType(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBIntListList request = message.get();
		CocoPacket resPacket = null;
		if (LogicApp.getInst().isCheckFanType()) {
			String[] reselt = CheckFanType.getInst().checkCoupleMj(request.getValueList().get(0).getValueList(),
					request.getValueList().get(1).getValueList(), request.getValueList().get(2).getValueList(),
					request.getValueList().get(3).getValueList());
			resPacket = new CocoPacket(RequestCode.CENTER_DEBUG_CHECK_FAN_TYPE_RES,
					CommonCreator.createPBIntString(Integer.valueOf(reselt[0]), reselt[1]), packet.getPlayerId());
		} else {
			resPacket = new CocoPacket(RequestCode.CENTER_DEBUG_CHECK_FAN_TYPE_RES,
					CommonCreator.createPBIntString(-1, "isCheckFanType not open"), packet.getPlayerId());
		}
		getCenterClient().sendRequest(resPacket);
	}
	
	private void clientArrayPai(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBIntString request = message.get();
		CocoPacket resPacket = null;
		try {
			if (!LogicApp.getInst().isArrayPai()) {
				resPacket = new CocoPacket(RequestCode.CENTER_DEBUG_ARRAY_PAI_RES,
						CommonCreator.createPBIntString(0, "isArrayPai not open"), packet.getPlayerId());
			} else {
				String value = request.getValue();
				if (value == null || value.equals("")) {
					ArrayPai.getInst().clean();
				} else {
					ArrayPai.getInst().arrayPai(GameType.getByValue(request.getKey()), request.getValue());
				}
				resPacket = new CocoPacket(RequestCode.CENTER_DEBUG_ARRAY_PAI_RES,
						CommonCreator.createPBIntString(1, "isArrayPai suc"), packet.getPlayerId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			resPacket = new CocoPacket(RequestCode.CENTER_DEBUG_ARRAY_PAI_RES,
					CommonCreator.createPBIntString(-1, "isArrayPai exception:" + e.getMessage()),
					packet.getPlayerId());
		}
		getCenterClient().sendRequest(resPacket);
	}

	private void actionReloadConfPersonalRoom(ChannelHandlerContext channelHandlerContext, CocoPacket cocoPacket, MessageHolder<MessageLite> messageLiteMessageHolder) {
		LogicActorManager.getDeskActor(0).put(() -> {
			PersonalConfRoomProvider.getInst().reLoad();
			return null;
		});
	}

//	private void actionDisbandeDesk(ChannelHandlerContext channelHandlerContext, CocoPacket cocoPacket, MessageHolder<MessageLite> messageLiteMessageHolder) {
//        int deskId = cocoPacket.getPlayerId();
//        Desk desk = DeskMgr.getInst().getDeskByDeskId(deskId);
//        if(desk == null){
//        	 logger.error("桌子已经解算{}", deskId);
//        	return;
//        }
//        if (desk instanceof ZjhDesk) {
//            ((ZjhDesk) desk).onGameEnd();
//            logger.info("移除zjh桌子{}", desk.getDeskId());
//        }else if (desk instanceof ZhuangNiuDesk) {
//        	 ((ZhuangNiuDesk) desk).onGameEnd();
//        }
//        
//        DeskMgr.getInst().removeDesk(desk);
//	}

	private void actionReloadConfRoom(ChannelHandlerContext channelHandlerContext, CocoPacket cocoPacket, MessageHolder<MessageLite> messageLiteMessageHolder) {
		LogicActorManager.getDeskActor(0).put(() -> {
			CoupleRoomInfoProvider.getInst().reLoad();
			ConfNiuProvider.getInst().reLoad();
			return null;
		});

	}

	private void actionFullPressure(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof ZjhDesk) {
			return;
		}
		ZjhDesk zjhDesk = (ZjhDesk) desk;
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			zjhDesk.fullPressure(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

//    private void actionALLGoldIn(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
//        Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
//        if (desk == null) {
//            return;
//        }
//        if (false == desk instanceof ZjhDesk) {
//            return;
//        }
//        ZjhDesk zjhDesk = (ZjhDesk) desk;
//        LogicActorManager.getDeskActor(desk.getPrivateDeskId()).put(() -> {
//            zjhDesk.allGoldIn(desk.getPlayerInfo(packet.getPlayerId()));
//            return null;
//        });
//    }

//	private void actionEnterZjhDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
//		Common.PBPlayerInfo playerInfo = message.get();
//		int deskId = packet.getPlayerId();
//		Desk desk = DeskMgr.getInst().getDeskByDeskId(deskId);
//		PlayerInfo info = new PlayerInfo();
//		info.setPlayerId(playerInfo.getPlayerId());
//		info.setCoin(playerInfo.getCoin());
//		info.setGender(Gender.getByValue(playerInfo.getGender()));
//		info.setIcon(playerInfo.getIcon());
//		info.setName(playerInfo.getName());
//		info.setProvince(playerInfo.getProvince());
//		info.setCity(playerInfo.getCity());
//		info.setDevice(playerInfo.getDevice());
//		info.setIp(playerInfo.getIp());
//		if (desk == null) {
//			//通知桌子不存在,重新匹配
//			logger.error("desk is not existd {}", info.getPlayerId());
//		} else {
//			LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
//				//进入桌子
//				ZjhDesk zjhDesk = (ZjhDesk) desk;
//				zjhDesk.enterPlayer(info);
//				return null;
//			});
//		}
//	}

	private void actionEnterGrabDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBPlayerInfo playerInfo = message.get();
		int deskId = packet.getPlayerId();
		Desk desk = DeskMgr.getInst().getDeskByDeskId(deskId);
		PlayerInfo info = new PlayerInfo();
		info.setPlayerId(playerInfo.getPlayerId());
		info.setCoin(playerInfo.getCoin());
		info.setGender(Gender.getByValue(playerInfo.getGender()));
		info.setIcon(playerInfo.getIcon());
		info.setName(playerInfo.getName());
		info.setProvince(playerInfo.getProvince());
		info.setCity(playerInfo.getCity());
		info.setDevice(playerInfo.getDevice());
		info.setIp(playerInfo.getIp());
		info.setPosition(playerInfo.getPosition());
		if (desk == null) {
			//通知桌子不存在,重新匹配
			logger.error("desk is not existd {}", info.getPlayerId());
			sendLogicDeskIsRemove(playerInfo.getPlayerId());
		} else {
			LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
				//进入桌子
				PokerDesk ndesk = (PokerDesk) desk;
				ndesk.enterPlayer(info);
				return null;
			});
		}
	}

	private void actionGrabNiuGrab(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBInt32 pos = message.get();
		int player = packet.getPlayerId();
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(player);
		if (desk == null) {
			//通知桌子不存在,重新匹配
			logger.error("desk is not existd {}", player);
		} else {
			LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
				ZhuangNiuDesk ndesk = (ZhuangNiuDesk) desk;
				ndesk.grab(ndesk.getPlayerInfo(player), pos.getValue());
				return null;
			});
		}
	}
	
	private void actionGrabNiuPlayerCal(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		int player = packet.getPlayerId();
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(player);
		if (desk == null) {
			//通知桌子不存在,重新匹配
			logger.error("desk is not existd {}", player);
		} else {
			LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
				ZhuangNiuDesk ndesk = (ZhuangNiuDesk) desk;
				ndesk.playerCal(ndesk.getPlayerInfo(player));
				return null;
			});
		}
	}

	private void actionGrabNiuAddBet(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBInt32 pos = message.get();
		int player = packet.getPlayerId();
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(player);
		if (desk == null) {
			//通知桌子不存在,重新匹配
			logger.error("desk is not existd {}", player);
		} else {
			LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
				ZhuangNiuDesk ndesk = (ZhuangNiuDesk) desk;
				ndesk.bet(ndesk.getPlayerInfo(player), pos.getValue());
				return null;
			});
		}
	}


	private void actionShowCards(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof ZjhDesk) {
			return;
		}
		ZjhDesk zjhDesk = (ZjhDesk) desk;
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			zjhDesk.showCars(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

	private void actionZjhGiveUp(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof ZjhDesk) {
			return;
		}
		ZjhDesk zjhDesk = (ZjhDesk) desk;
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			zjhDesk.giveUp(desk.getPlayerInfo(packet.getPlayerId()), false);
			return null;
		});
	}

	private void actionZjhCompareCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof ZjhDesk) {
			return;
		}
		Common.PBInt32 request = message.get();
		ZjhDesk zjhDesk = (ZjhDesk) desk;
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			zjhDesk.compareCards(desk.getPlayerInfo(packet.getPlayerId()), request.getValue());
			return null;
		});
	}

	private void actionZjhLookCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof ZjhDesk) {
			return;
		}
		ZjhDesk zjhDesk = (ZjhDesk) desk;
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			zjhDesk.lookCard(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

	private void actionZjhAddGold(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (!(desk instanceof ZjhDesk)) {
			return;
		}
		Common.PBInt32 request = message.get();
		ZjhDesk zjhDesk = (ZjhDesk) desk;
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			zjhDesk.addPots(desk.getPlayerInfo(packet.getPlayerId()), request.getValue());
			return null;
		});
	}

	private void actionDdzTouguan(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> messag) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof DdzDesk) {
			return;
		}
		DdzDesk ddzDesk = (DdzDesk) (desk);
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			ddzDesk.tuoguanGame(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

	private void actionDdzCancelTuoguan(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> messag) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof DdzDesk) {
			return;
		}
		DdzDesk ddzDesk = (DdzDesk) (desk);
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			ddzDesk.cancelTuoguan(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}


	private void actionDdzPass(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof DdzDesk) {
			return;
		}
		DdzDesk ddzDesk = (DdzDesk) (desk);
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			ddzDesk.passCard(desk.getPlayerInfo(packet.getPlayerId()),-1);
			return null;
		});
	}


	private void actionDdzDiscardCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof DdzDesk) {
			return;
		}
		Common.PBInt32List request = message.get();
		DdzDesk ddzDesk = (DdzDesk) (desk);
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			ddzDesk.discardCard(desk.getPlayerInfo(packet.getPlayerId()), request.getValueList(),-1);
			return null;
		});
	}

	private void actionJiabei(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof DdzDesk) {
			return;
		}
		Common.PBInt32 request = message.get();
		DdzDesk ddzDesk = (DdzDesk) (desk);
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
//			ddzDesk.jiabei(desk.getPlayerInfo(packet.getPlayerId()), request.getValue() == 1 ? true : false);
			return null;
		});
	}


	private void actionRobLord(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof DdzDesk) {
			return;
		}
		Common.PBInt32 request = message.get();
		DdzDesk ddzDesk = (DdzDesk) (desk);
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			ddzDesk.robLord(desk.getPlayerInfo(packet.getPlayerId()), request.getValue() == 1 ? true : false);
			return null;
		});
	}


	private void actionPlayerMoneyChange(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		Common.PBPair request = message.get();
		int coin = request.getKey();
		boolean add = request.getValue() == 1;
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			desk.playerMoneyChange(packet.getPlayerId(), coin, add);
			return null;
		});
	}

	private void actionReloadDynamicProperties(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		LogicActorManager.getDeskActor(0).put(() -> {
			DynamicInfoProvider.getInst().reLoad();
			return null;
		});
	}

	private void actionGetNiuniuRankList(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof NiuNiuDesk == false) {
			return;
		}
		NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
		LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
			niuDesk.getNiuniuDeskRank(niuDesk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});

	}

	private void actionContinueGame(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			desk.playerWantContinue(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

	private void actionUpdateMoney(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			desk.getPlayerInfo(packet.getPlayerId()).setCoin(request.getValue());
			if (desk instanceof AbstractDesk) {  
				AbstractDesk mjDesk = (AbstractDesk) desk;
				mjDesk.syncAllPlayerMoney();
			}
			return null;
		});
	}

	private void actionAdmitDefeat(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof SCMJDesk == false) {
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			((SCMJDesk) desk).playerAdmitDefeat(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

	private void actionDisbandRoomDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBInt32 request = message.get();
		Desk desk = DeskMgr.getInst().getDeskByDeskId(request.getValue());
		if (desk == null) {
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			if (desk instanceof MJDesk) {
				((MJDesk) desk).disbandDesk();
			}
			if (desk instanceof ZjhDesk) {
				((ZjhDesk) desk).disbandDesk();
			}
			if (desk instanceof ZhuangNiuDesk) {
				((ZhuangNiuDesk) desk).disbandDesk();
			}
			return null;
		});
	}


	private void actioRoomGameStart(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBInt32 request = message.get();
		Desk desk = DeskMgr.getInst().getDeskByDeskId(request.getValue());
		if (desk == null) {
			return;
		}
		if (false == desk instanceof MJDesk) {
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			((MJDesk) desk).startNewGame();
			return null;
		});
	}


	private List<PlayerInfo> getPlayerList(Common.PBPlayerInfoList playerList) {
		List<PlayerInfo> result = new ArrayList<>();
		Common.PBPlayerInfoList list = playerList;
		list.getPlayerInfoListList().forEach(e -> {
			PlayerInfo info = new PlayerInfo();
			info.setPlayerId(e.getPlayerId());
			info.setCoin(e.getCoin());
			info.setGender(Gender.getByValue(e.getGender()));
			info.setIcon(e.getIcon());
			info.setName(e.getName());
			info.setPosition(e.getPosition());
			info.setProvince(e.getProvince());
			info.setCity(e.getCity());
			info.setChannel_id(e.getChannelId());
			info.setPackage_id(e.getPackageId());
			info.setDevice(e.getDevice());
			info.setIp(e.getIp());
			result.add(info);
		});
		return result;
	}

	private void actionCreateRoomDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		CoupleMajiang.PBLoigcCreareRoomReq request = message.get();
		Lobby.PBCreateDeskReq deskReq = request.getReq();
		GameType type = GameType.getByValue(deskReq.getGameId());
		if (type == null) {
			return;
		}
		List<PlayerInfo> playerList = getPlayerList(request.getPlayerList());
		if (playerList.size() == 0) {
			logger.debug(" the player size is illegal ");
			return;
		}
		int creatorId = playerList.get(0).getPlayerId();
		Desk desk = null;
		switch (type) {
			case XUELIU:
				Lobby.PBCreateXueNiuReq xueNiuReq = deskReq.getRequest();
				desk = new XueNiuDesk(creatorId, 99999, request.getRoomId(), playerList, xueNiuReq.getChange(), xueNiuReq.getTrans()
						, DealZimo.getByValue(xueNiuReq.getDealZimo()), xueNiuReq.getDiangangZimo(), BonusType.getByValue(xueNiuReq.getMaxFan()).getTimes()
						, xueNiuReq.getExreaFanList(), xueNiuReq.getBaseScore(), xueNiuReq.getEnterTimes());
				break;
			case XUEZHAN:
				Lobby.PBCreateXueNiuReq xuezhanReq = deskReq.getRequest();
				desk = new XuezhanDesk(creatorId, 99999, request.getRoomId(), playerList, xuezhanReq.getChange(), xuezhanReq.getTrans()
						, DealZimo.getByValue(xuezhanReq.getDealZimo()), xuezhanReq.getDiangangZimo(), BonusType.getByValue(xuezhanReq.getMaxFan()).getTimes()
						, xuezhanReq.getExreaFanList(), xuezhanReq.getBaseScore(), xuezhanReq.getEnterTimes());
				break;
			case ZJH:
				Lobby.PBCreateZjhReq zjhReq = deskReq.getZjhReq();
				desk = new ZjhDesk(creatorId, zjhReq.getRoundsNum(), request.getRoomId(), playerList, zjhReq.getLimitPots(), zjhReq.getEnterPots(), zjhReq.getMaxRound(), zjhReq.getCompareRound(), zjhReq.getLookRound(), zjhReq.getFullRound());
				break;
			case CLASS_NIU:
				Lobby.PBCreateNiuReq niuniuReq = deskReq.getNiuReq();
				desk = new ClassZhuangNiuDesk(niuniuReq.getModel(),creatorId, 99999, request.getRoomId(), playerList, niuniuReq.getBaseScore(), niuniuReq.getEnterTimes(),niuniuReq.getRule(),niuniuReq.getMulit());
				break;
		}
		DeskMgr.getInst().registerDesk(desk);
		//注册之后通知center  桌子创建好了                        要带过去的信息是什么呢, 玩家的 哪几个玩家, 在哪些位置
		CocoPacket resPacket = null;
		if (desk instanceof MJDesk) {
			MJDesk mjDesk = (MJDesk) desk;
			resPacket = new CocoPacket(RequestCode.CENTER_CREATE_ROOM_DESK_SUCC, CoupleCreator.createPBPlayerInfoList(mjDesk.getPlayer2InfoMap()), mjDesk.getDeskId());
		} else if (desk instanceof ZjhDesk) {
			ZjhDesk zjhDesk = (ZjhDesk) desk;
			resPacket = new CocoPacket(RequestCode.CENTER_CREATE_ROOM_DESK_SUCC, CoupleCreator.createPBPlayerInfoListZjh(zjhDesk.getDeskInfoMap()), zjhDesk.getDeskId());
		} else if (desk instanceof ZhuangNiuDesk) {
			ZhuangNiuDesk niuDesk = (ZhuangNiuDesk) desk;
			resPacket = new CocoPacket(RequestCode.CENTER_CREATE_ROOM_DESK_SUCC, CoupleCreator.createPBPlayerInfoListGradNiu(niuDesk.getDeskInfoMap()), niuDesk.getDeskId());
		}
		getCenterClient().sendRequest(resPacket);
	}


	private void actionPlayerLogout(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			desk.playerLogout(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}


	private void actionLeaveDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			desk.playerLeave(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}


	private void actionResetNiuniu(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if(desk instanceof NiuNiuDesk){
			NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
			LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
				niuDesk.resetGameForPlayer(niuDesk.getPlayerInfo(packet.getPlayerId()));
				return null;
			});
		}
	}

	private void actionGetHisRecord(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if(desk instanceof NiuNiuDesk){
			NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
			LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
				niuDesk.getNiuniuHistoryRecord(niuDesk.getPlayerInfo(packet.getPlayerId()));
				return null;
			});
		}
	}


	private void actionCancelBanner(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if(desk instanceof NiuNiuDesk){
			NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
			LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
				niuDesk.playerCancelBanner(niuDesk.getPlayerInfo(packet.getPlayerId()));
				return null;
			});
		}
	}

	private void actionGiveUpBanner(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if(desk instanceof NiuNiuDesk){
			NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
			LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
				niuDesk.playerGiveUpBanner(niuDesk.getPlayerInfo(packet.getPlayerId()));
				return null;
			});
		}
	}


	private void actionRobBanner(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if(desk instanceof NiuNiuDesk){
			NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
			LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
				niuDesk.playerRobZhuang(niuDesk.getPlayerInfo(packet.getPlayerId()));
				return null;
			});
		}
	}

	private void actionNiuNiuChip(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBPair request = message.get();
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if(desk instanceof NiuNiuDesk){
			NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
			LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
				niuDesk.playerChip(niuDesk.getPlayerInfo(packet.getPlayerId()), request.getKey(), request.getValue());
				return null;
			});
		}
	}

	private void actionNiuNiuRenewed(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Common.PBPairList reqeust = message.get();
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if(desk instanceof NiuNiuDesk){
			NiuNiuDesk niuDesk = (NiuNiuDesk) desk;
			LogicActorManager.getDeskActor(niuDesk.getDeskId()).put(() -> {
				niuDesk.playerRenewad(niuDesk.getPlayerInfo(packet.getPlayerId()), reqeust.getListList());
				return null;
			});
		}
	}

	private void actionEnterNiuniuDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		CoupleMajiang.PBCreateDesk deskBuilder = message.get();
		
		int deskId = packet.getPlayerId();
		LogicActorManager.getDeskActor(deskId).put(() -> {
			Common.PBPlayerInfo playerInfo = deskBuilder.getPlayerList().getPlayerInfoList(0);
			int playerId = playerInfo.getPlayerId();
			
			Desk desk = DeskMgr.getInst().getDeskByDeskId(deskId);
			PlayerInfo info = null;
			if (desk != null) {
				info = desk.getPlayerInfo(playerId);
			}else{
				desk = new NiuNiuDesk(deskBuilder.getRoomId(),deskId);
				DeskMgr.getInst().registerDesk(desk);
			}
			if (info == null) {
				info = new PlayerInfo();
				info.setPlayerId(playerInfo.getPlayerId());
				info.setCoin(playerInfo.getCoin());
				info.setGender(Gender.getByValue(playerInfo.getGender()));
				info.setIcon(playerInfo.getIcon());
				info.setName(playerInfo.getName());
				info.setProvince(playerInfo.getProvince());
				info.setCity(playerInfo.getCity());
				info.setChannel_id(playerInfo.getChannelId());
				info.setPackage_id(playerInfo.getPackageId());
				info.setIp(playerInfo.getIp());
				info.setDevice(playerInfo.getDevice());
				info.setIp(playerInfo.getIp());
			}
			DeskMgr.getInst().registerDesk(info.getPlayerId(), desk);
			((NiuNiuDesk) desk).playerEnterDesk(info);
			return null;
		});
	}

	private void actionChoseType(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof SCMJDesk == false) {
			return;
		}
		SCMJDesk mjDesk = (SCMJDesk) desk;
		Common.PBInt32 request = message.get();
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.playerChoseType(mjDesk.getPlayerInfo(packet.getPlayerId()), request.getValue());
			return null;
		});
	}

	private void actionChoseSwitchCards(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof SCMJDesk == false) {
			return;
		}
		SCMJDesk mjDesk = (SCMJDesk) desk;
		Common.PBInt32List request = message.get();
		if (request.getValueList().size() != 3) {
			return;
		}
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.switchXueniuCards(mjDesk.getPlayerInfo(packet.getPlayerId()), request.getValueList());
			return null;
		});
	}

	private void actionResetGame(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			sendLogicDeskIsRemove(packet.getPlayerId());
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			PlayerInfo player = desk.getPlayerInfo(packet.getPlayerId());
			if(player == null){
				sendLogicDeskIsRemove(packet.getPlayerId());
				return null;
			}
			desk.playerReLogin(player);
			return null;
		});
	}


	private void actionDoTing(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		Common.PBInt32 request = message.get();
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.doTing(mjDesk.getPlayerInfo(packet.getPlayerId()), request.getValue());
			return null;
		});
	}

	private void actionGuo(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.guoPai(mjDesk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}


	private void actionHuCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		CoupleMajiang.PBHuReq request = message.get();
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.hupaiRequest(mjDesk.getPlayerInfo(packet.getPlayerId()), request.getChiListList(), request.getKeListList(), request.getJiangValueList());
			return null;
		});
	}

	private void actionTingCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		CoupleMajiang.PBTingReq request = message.get();
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.tingpai(mjDesk.getPlayerInfo(packet.getPlayerId()), request);
			return null;
		});
	}

	private void actionGangCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		CoupleMajiang.PBGangReq req = message.get();
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.gangPai(mjDesk.getPlayerInfo(packet.getPlayerId()), req.getCardValue());
			return null;
		});
	}

	private void actionKeCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.pengpai(mjDesk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

	private void actionChiCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		CoupleMajiang.PBChiReq request = message.get();
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.chiPai(mjDesk.getPlayerInfo(packet.getPlayerId()), request.getPosition());
			return null;
		});
	}

	private void actionDiscardCard(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		CoupleMajiang.PBDiscardCardReq request = message.get();
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk == false) {
			return;
		}
		MJDesk mjDesk = (MJDesk) desk;
		LogicActorManager.getDeskActor(mjDesk.getDeskId()).put(() -> {
			mjDesk.discardCard(mjDesk.getPlayerInfo(packet.getPlayerId()), request.getCardValue());
			return null;
		});
	}

	private void actionReady(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(packet.getPlayerId());
		if (desk == null) {
			logger.debug(" the desk {} is null ", packet.getPlayerId());
			return;
		}
		LogicActorManager.getDeskActor(desk.getDeskId()).put(() -> {
			desk.playerReady(desk.getPlayerInfo(packet.getPlayerId()));
			return null;
		});
	}

	private void actionCreateDesk(ChannelHandlerContext client, CocoPacket packet, MessageHolder<MessageLite> message) {
		CoupleMajiang.PBCreateDesk request = message.get();
		int roomId = request.getRoomId();
		int deskId = request.getDeskId();
		GameType type = GameType.getByValue(request.getGameType());
		if (type == null) {
			return;
		}
		List<PlayerInfo> playerList = getPlayerList(request.getPlayerList());
		Desk desk = null;
		CoupleRoom room = CoupleRoomInfoProvider.getInst().getRoomConf(roomId);
		switch (type) {
			case COUPLE_MJ:
				desk = new CoupleMJDesk(deskId,roomId, playerList);
				if (room.getClassify() == 2) {
					((MJDesk) desk).setTiyan(true);
				}
				
				break;
			case XUELIU:
				desk = new XueNiuDesk(deskId,roomId, playerList);
				break;
			case XUEZHAN:
				desk = new XuezhanDesk(deskId,roomId, playerList);
				break;
			case DDZ:
				desk = new DdzDesk(deskId,roomId, playerList);
				break;
			case ZJH:
				desk = new ZjhDesk(deskId, roomId, playerList);
				break;
			case COUPLE_DDZ:
				desk = new CoupleDdzDesk(deskId,roomId, playerList);
				break;
			case LZ_DDZ:
				desk = new LzDdzDesk(deskId,roomId, playerList);
				break;
			case GRAB_NIU:
				desk = new GrabZhuangNiuDesk(deskId, roomId, playerList);
				break;
			case CLASS_NIU:
				desk = new ClassZhuangNiuDesk(deskId, roomId, playerList);
				break;
		}

		logger.info("创建桌子{} 成功,id为 {}", type, desk.getDeskId());

		DeskMgr.getInst().registerDesk(desk);
		//注册之后通知center  桌子创建好了                        要带过去的信息是什么呢, 玩家的 哪几个玩家, 在哪些位置
		CocoPacket resPacket = null;
		if (desk instanceof MJDesk) {
			MJDesk mjDesk = (MJDesk) desk;
			resPacket = new CocoPacket(RequestCode.CENTER_CREATE_DESK_SUCC, CoupleCreator.createPBPlayerInfoList(mjDesk.getPlayer2InfoMap()), mjDesk.getDeskId());
		} else if (desk instanceof DdzDesk) {
			DdzDesk ddzDesk = (DdzDesk) desk;
			resPacket = new CocoPacket(RequestCode.CENTER_CREATE_DESK_SUCC, CoupleCreator.createPBPlayerInfoListDdz(ddzDesk.getDeskInfoMap()), ddzDesk.getDeskId());
		} else if (desk instanceof ZjhDesk) {
			ZjhDesk zjhDesk = (ZjhDesk) desk;
			resPacket = new CocoPacket(RequestCode.CENTER_CREATE_ZJH_DESK_SUCC, CoupleCreator.createPBPlayerInfoListZjh(zjhDesk.getDeskInfoMap()), zjhDesk.getDeskId());
		} else if (desk instanceof ZhuangNiuDesk) {
			ZhuangNiuDesk niuDesk = (ZhuangNiuDesk) desk;
			resPacket = new CocoPacket(RequestCode.CENTER_CREATE_GRAD_NIU_DESK_SUCC, CoupleCreator.createPBPlayerInfoListGradNiu(niuDesk.getDeskInfoMap()), niuDesk.getDeskId());
		}
		getCenterClient().sendRequest(resPacket);
	}

	@Override
	public void handPacket(ChannelHandlerContext client, CocoPacket packet) {
		RequestCode reqCode = packet.getReqCode();
		if (reqCode.getSendTo() != getAppId()) {
			getCenterClient().sendRequest(packet);
		} else {
			Pair<MessageLite, IActionHandler> messageAndHandler = actionHandlers.get(packet.getReqId());
			if (messageAndHandler == null) {
				logger.debug(" the mssage hand is null and the req code is {}", reqCode);
			} else {
				IActionHandler handler = messageAndHandler.getRight();
				MessageLite protoType = messageAndHandler.getLeft();
				if (handler != null) {
					MessageLite message = null;
					try {
						message = protoType == null ? null : protoType.getParserForType().parseFrom(packet.getBytes());
						LogUtil.msgLogger.info("player {} , request:{}, packet {}", new Object[]{packet.getPlayerId(), reqCode, message});
					} catch (InvalidProtocolBufferException e) {
						logger.error("exception; {}", e);
					}
					handler.doAction(client, packet, new MessageHolder<>(message));
				}
			}
		}
	}
	
	private void sendLogicDeskIsRemove(int playerId){
		getCenterClient().sendRequest(new CocoPacket(RequestCode.CENTER_PLAYER_DESK_IS_REMOVE,null,playerId));
	}

	@Override
	protected AppId getAppId() {
		return AppId.LOGIC;
	}

	@Override
	protected NetClient getCenterClient() {
		return LogicApp.getInst().getClient();
	}
}
