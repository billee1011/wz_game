package protobuf.creator;

import java.util.List;
import java.util.Map;

import logic.define.GameType;
import logic.majiong.CoupleFanType;
import logic.majiong.CoupleMJDesk;
import logic.majiong.GameConst;
import logic.majiong.GameUtil;
import logic.majiong.MJDesk;
import logic.majiong.MajiangOperationType;
import logic.majiong.MajongType;
import logic.majiong.PlayerDeskInfo;
import logic.majiong.PlayerInfo;
import logic.majiong.SCMJDesk;
import logic.majiong.XueNiuDesk;
import logic.majiong.XuezhanDesk;
import logic.majiong.define.MJPosition;
import logic.majiong.xueniu.XNOneCalRecord;
import logic.poker.ddz.DdzDeskInfo;
import logic.poker.niuniu.zhuang.RoomNiuNiuDeskInfo;
import logic.poker.zjh.ZjhDeskInfo;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.Xueniu;
import util.MiscUtil;
import util.Pair;
import util.WordBadUtil;

public class CoupleCreator {
	public static CoupleMajiang.PBWatOperationRes createPBWatOperationRes(int playerId, List<Integer> operationList, int huValue) {
		CoupleMajiang.PBWatOperationRes.Builder builder = CoupleMajiang.PBWatOperationRes.newBuilder();
		if (operationList != null) {
			operationList.forEach(e -> {
				builder.addOperationList(e);
				if (e.intValue() == MajiangOperationType.HU.getValue()) {
					if (huValue != 0) {
						builder.addValue(huValue);
					}
				}
			});
		}
		builder.setPlayerId(playerId);
		return builder.build();
	}

	public static CoupleMajiang.PBDealOneCardRes createPBDealOneCardRes(int playerId, List<Integer> list) {
		CoupleMajiang.PBDealOneCardRes.Builder builder = CoupleMajiang.PBDealOneCardRes.newBuilder();
		builder.setPlayerId(playerId);
		if (list != null) {
			list.forEach(e -> builder.addPai(e));
		}
		return builder.build();
	}

	public static CoupleMajiang.PBHupaiRes createPBHupaiRes(int playerId, List<Integer> paiList, int huValue, List<CoupleFanType> fanList
			, List<Pair<Integer, Integer>> coinList, int totalFan, List<Integer> handCards, String game_no) {
		CoupleMajiang.PBHupaiRes.Builder builder = CoupleMajiang.PBHupaiRes.newBuilder();
		builder.setPlayerId(playerId);
		builder.setHuValue(huValue);
		if (coinList != null) {
			coinList.forEach(e -> builder.addTotalCoin(CommonCreator.createPBPair(e.getLeft(), e.getRight())));
		}
		paiList.forEach(e -> builder.addPaiList(GameUtil.getRealValue(e)));
		fanList.forEach(e -> builder.addFuList(e.getValue()));
		builder.setTotalFan(totalFan);
		handCards.forEach(e -> builder.addHandCards(e));
		builder.setGangShangHua(false);
		builder.setGameNo(game_no);
		return builder.build();
	}


	public static CoupleMajiang.PBTingFanRes createPBTingFanRes(List<Pair<Integer, List<Pair<Integer, Integer>>>> fanList) {
		CoupleMajiang.PBTingFanRes.Builder builder = CoupleMajiang.PBTingFanRes.newBuilder();
		fanList.forEach(e -> builder.addItem(createPBTingFanItem(e.getLeft(), e.getRight())));
		return builder.build();
	}

	public static CoupleMajiang.PBTingFanItem createPBTingFanItem(int dropCard, List<Pair<Integer, Integer>> fanList) {
		CoupleMajiang.PBTingFanItem.Builder builder = CoupleMajiang.PBTingFanItem.newBuilder();
		builder.setOutCard(dropCard);
		fanList.forEach(e -> builder.addFan(CommonCreator.createPBPair(e.getLeft(), e.getRight())));
		return builder.build();
	}

	public static CoupleMajiang.PBCoupleResetGame createPBCoupleResetGame(MJDesk mjdesk, PlayerInfo player, boolean end, int posValue, List<Integer> ops) {
		CoupleMajiang.PBCoupleResetGame.Builder builder = CoupleMajiang.PBCoupleResetGame.newBuilder();
		builder.setPlayerInfoList(createPBPlayerInfoList(mjdesk.getPlayer2InfoMap()));
		builder.setRounds(mjdesk.getRounds());
		if (mjdesk.getPlayerDeskInfo(player).isReady()&& !mjdesk.isGameing()){ // 應客戶端要求結束后 斷綫不發送牌局信息
			builder.setZhuangPlayerId(0);
			builder.setDiceOne(0);
			builder.setDiceTwo(0);
			builder.setCurrentPos(0);
			return builder.build();
		}
		builder.setDiceOne(mjdesk.getDiceNum1());
		builder.setDiceTwo(mjdesk.getDiceNum2());
		builder.setZhuangPlayerId(mjdesk.getZhuangId());
		if (mjdesk.getCurrentTurn() == null) {
			builder.setCurrentPos(0);
		} else {
			builder.setCurrentPos(mjdesk.getCurrentTurn().getValue());
		}
		for (Map.Entry<PlayerInfo, PlayerDeskInfo> entry : mjdesk.getPlayer2InfoMap().entrySet()) {
			if (entry.getKey() == player) {
				builder.addDeskInfoList(createPBOneOppositeInfo(entry.getValue(), true, end, mjdesk.getGameType().getValue()));
			} else {
				builder.addDeskInfoList(createPBOneOppositeInfo(entry.getValue(), false, end, mjdesk.getGameType().getValue()));
			}
		}
		if (mjdesk instanceof XueNiuDesk) {
			XueNiuDesk desk = (XueNiuDesk) mjdesk;
			for (List<XNOneCalRecord> recordList : desk.getCalculateMap().values()) {
				recordList.forEach(e -> builder.addCalList(e.getCal()));
			}
		} else if (mjdesk instanceof XuezhanDesk) {
			XuezhanDesk desk = (XuezhanDesk) mjdesk;
			for (List<XNOneCalRecord> recordList : desk.getCalculateMap().values()) {
				recordList.forEach(e -> builder.addCalList(e.getCal()));
			}
		}
		if (mjdesk instanceof CoupleMJDesk) {
			CoupleMJDesk desk = (CoupleMJDesk) mjdesk;
			CoupleMajiang.PBHupaiRes res = desk.getHuRes(player);
			if (res != null) {
				builder.setHupaiRes(res);
			}
		}

		if (mjdesk instanceof SCMJDesk) {
			SCMJDesk desk = (SCMJDesk) mjdesk;
			Xueniu.PBXueNiuTotalCalculate res = desk.getCalCulate(player);
			if (res != null) {
				builder.setXueliuRes(res);
			}
		}

		if (mjdesk instanceof SCMJDesk) {
			SCMJDesk scDesk = (SCMJDesk) mjdesk;
			if (scDesk.isChoseTypeEnd()) {
				for (Map.Entry<MJPosition, MajongType> entry : scDesk.getPlayerChoseType().entrySet()) {
					builder.addQueState(CommonCreator.createPBPair(entry.getKey().getValue(), entry.getValue().getValue()));
				}
			} else {
				for (Map.Entry<MJPosition, MajongType> entry : scDesk.getPlayerChoseType().entrySet()) {
					if (entry.getKey().getValue() == player.getPosition()) {
						builder.addQueState(CommonCreator.createPBPair(entry.getKey().getValue(), entry.getValue().getValue()));
					}
				}
			}
			List<Integer> switchCards = scDesk.getSwitchCards(player);
			if (switchCards != null) {
				switchCards.forEach(e -> builder.addSwitchCards(e));
			}
			builder.setSwitchEnd(scDesk.isSwitchEnd());
		}

		if (mjdesk.getCurrOperationTurn() != null && mjdesk.getCurrOperationTurn().getValue() == posValue) {
			if (ops != null) {
				builder.setOperators(CommonCreator.createPBInt32List(ops));
				if (ops.contains(MajiangOperationType.HU.getValue())) {
					int value = 0;
					try {
						value = mjdesk.getHuValue(player);
					} finally {
						builder.setHuValue(value);
					}
				}
			}
		}

		if(mjdesk.opBeginTime!=0){
			builder.setOperatorTime(MiscUtil.getCurrentSeconds() - mjdesk.opBeginTime);
		}

		return builder.build();
	}

	public static CoupleMajiang.PBOneOppositeInfo createPBOneOppositeInfo(PlayerDeskInfo info, boolean self, boolean end, int gameId) {
		CoupleMajiang.PBOneOppositeInfo.Builder builder = CoupleMajiang.PBOneOppositeInfo.newBuilder();
		builder.setTingpai(info.isBaoTing());
		info.getResetChiData().forEach(e -> builder.addChiList(CommonCreator.createPBPair(e.getLeft(), e.getRight())));
		for (Integer value : info.getGangQueue()) {
			if (self) {
				builder.addGangList(value);
			} else {
				if (value < GameConst.AN_MASK) {
					builder.addGangList(value);
				} else {
					if (end || gameId != GameType.COUPLE_MJ.getValue()) {
						builder.addGangList(value);
					} else {
						builder.addGangList(0);
					}
				}
			}
		}
		builder.setAngangCount(0);
		info.getHandCards().forEach(e -> {
			if (end) {
				builder.addHandCards(e);
			} else {
				if (self) {
					builder.addHandCards(e);
				} else {
					builder.addHandCards(0);
				}
			}
		});
		builder.setPosition(info.getPosition().getValue());
		info.getKeQueue().forEach(e -> builder.addKeList(e));
		info.getHuaCards().forEach(e -> builder.addHuaCards(e));
		info.getDeskPaiStack().forEach(e -> builder.addOutCards(e));
		info.getHuPaiList().forEach(e -> builder.addHuCards(e));
		builder.setDefeat(info.isDefeat());
		return builder.build();
	}

	public static CoupleMajiang.PBCoupleMJStartRes createPBCoupleMJStartRes(MJDesk desk, PlayerDeskInfo info) {
		CoupleMajiang.PBCoupleMJStartRes.Builder builder = CoupleMajiang.PBCoupleMJStartRes.newBuilder();
		builder.setDiceOne(desk.getDiceNum1());
		builder.setDiceTwo(desk.getDiceNum2());
		builder.setZhuangPlayerId(desk.getZhuangId());
		info.getHandCards().forEach(e -> builder.addSelfHandCards(e));
		desk.getPlayerDeskInfList().forEach(e -> builder.addHuaCardList(createPBPlayerHuapaiList(e)));
		return builder.build();
	}

	public static CoupleMajiang.PBPlayerHuapaiList createPBPlayerHuapaiList(PlayerDeskInfo info) {
		CoupleMajiang.PBPlayerHuapaiList.Builder builder = CoupleMajiang.PBPlayerHuapaiList.newBuilder();
		builder.setPosition(info.getPosition().getValue());
		info.getHuaCards().forEach(e -> builder.addHuaList(e));
		return builder.build();
	}

	public static Common.PBPlayerInfoList createPBPlayerInfoList(Map<PlayerInfo, PlayerDeskInfo> infoMap) {
		Common.PBPlayerInfoList.Builder builder = Common.PBPlayerInfoList.newBuilder();
		infoMap.forEach((e, f) -> builder.addPlayerInfoList(createPBPlayerInfo(e, f)));
		return builder.build();
	}

	public static Common.PBPlayerInfoList createPBPlayerInfoListDdz(Map<PlayerInfo, DdzDeskInfo> infoMap) {
		Common.PBPlayerInfoList.Builder builder = Common.PBPlayerInfoList.newBuilder();
		infoMap.forEach((e, f) -> builder.addPlayerInfoList(createPBPlayerInfo(e, f)));
		return builder.build();
	}


	public static Common.PBPlayerInfoList createPBPlayerInfoListZjh(Map<PlayerInfo, ZjhDeskInfo> infoMap) {
		Common.PBPlayerInfoList.Builder builder = Common.PBPlayerInfoList.newBuilder();
		infoMap.forEach((e, f) -> builder.addPlayerInfoList(createZjhPlayerInfo(e, f)));
		return builder.build();
	}

	public static Common.PBPlayerInfoList createPBPlayerInfoListGradNiu(Map<PlayerInfo, RoomNiuNiuDeskInfo> infoMap) {
		Common.PBPlayerInfoList.Builder builder = Common.PBPlayerInfoList.newBuilder();
		infoMap.forEach((e, f) -> builder.addPlayerInfoList(createGrabNiuPlayerInfo(e, f)));
		return builder.build();
	}

	public static Common.PBPlayerInfo createPBPlayerInfo(PlayerInfo player, DdzDeskInfo info) {
		Common.PBPlayerInfo.Builder builder = Common.PBPlayerInfo.newBuilder();
		builder.setPlayerId(player.getPlayerId());
		builder.setCoin((int) player.getCoin());
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		builder.setGender(player.getGender().getValue());
		builder.setPosition(info.getPositionValue());
//		if(WordBadUtil.hasBadProvince(player.getProvince())){
//			builder.setProvince(WordBadUtil.DEFAULT_PROVINCE);
//			builder.setCity(WordBadUtil.DEFAULT_CITY);
//		}else{
			builder.setProvince(player.getProvince());
			builder.setCity(player.getCity());
//		}
		builder.setChannelId(player.getChannel_id());
		builder.setPackageId(player.getPackage_id());
		builder.setDevice(player.getDevice());
		builder.setIsLeave(false);
		builder.setIsLeave(info.isLeave());
		builder.setChannelId(player.getChannel_id());
		builder.setPackageId(player.getPackage_id());
		builder.setDevice(player.getDevice());
		builder.setIp(player.getIp());
		return builder.build();
	}

	public static Common.PBPlayerInfo createPBPlayerInfo(PlayerInfo player, PlayerDeskInfo info) {
		Common.PBPlayerInfo.Builder builder = Common.PBPlayerInfo.newBuilder();
		builder.setPlayerId(player.getPlayerId());
		builder.setCoin((int) player.getCoin());
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		builder.setGender(player.getGender().getValue());
		builder.setPosition(info.getPositionValue());
//		if(WordBadUtil.hasBadProvince(player.getProvince())){
//			builder.setProvince(WordBadUtil.DEFAULT_PROVINCE);
//			builder.setCity(WordBadUtil.DEFAULT_CITY);
//		}else{
			builder.setProvince(player.getProvince());
			builder.setCity(player.getCity());
//		}
		builder.setIsLeave(false);
		if (true == info.isLeave() || true == info.isLogout()) {
			builder.setIsLeave(true);
		}
		builder.setChannelId(player.getChannel_id());
		builder.setPackageId(player.getPackage_id());
		builder.setDevice(player.getDevice());
		builder.setIp(player.getIp());
		builder.setIsReady(info.isReady());
		return builder.build();
	}

	public static Common.PBPlayerInfo createZjhPlayerInfo(PlayerInfo player, ZjhDeskInfo info) {
		Common.PBPlayerInfo.Builder builder = Common.PBPlayerInfo.newBuilder();
		builder.setPlayerId(player.getPlayerId());
		builder.setCoin((int) player.getCoin());
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		builder.setGender(player.getGender().getValue());
		builder.setPosition(info.getPositionValue());
//		if(WordBadUtil.hasBadProvince(player.getProvince())){
//			builder.setProvince(WordBadUtil.DEFAULT_PROVINCE);
//			builder.setCity(WordBadUtil.DEFAULT_CITY);
//		}else{
			builder.setProvince(player.getProvince());
			builder.setCity(player.getCity());
//		}
		builder.setIsLeave(info.isLeave());
		builder.setChannelId(player.getChannel_id());
		builder.setPackageId(player.getPackage_id());
		builder.setDevice(player.getDevice());
		builder.setIp(player.getIp());
		builder.setIsWacth(info.isWatch());
		builder.setIsReady(info.isReady());
		return builder.build();
	}

	public static Common.PBPlayerInfo createGrabNiuPlayerInfo(PlayerInfo player, RoomNiuNiuDeskInfo info) {
		Common.PBPlayerInfo.Builder builder = Common.PBPlayerInfo.newBuilder();
		builder.setPlayerId(player.getPlayerId());
		builder.setCoin((int) player.getCoin());
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		builder.setGender(player.getGender().getValue());
		builder.setPosition(info.getPositionValue());
//		if(WordBadUtil.hasBadProvince(player.getProvince())){
//			builder.setProvince(WordBadUtil.DEFAULT_PROVINCE);
//			builder.setCity(WordBadUtil.DEFAULT_CITY);
//		}else{
			builder.setProvince(player.getProvince());
			builder.setCity(player.getCity());
//		}
		builder.setIsLeave(info.isLeave());
		builder.setChannelId(player.getChannel_id());
		builder.setPackageId(player.getPackage_id());
		builder.setDevice(player.getDevice());
		builder.setIp(player.getIp());
		builder.setIsWacth(info.isWatch());
		builder.setIsReady(info.isReady());
		return builder.build();
	}
}
