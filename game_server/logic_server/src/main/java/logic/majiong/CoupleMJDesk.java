package logic.majiong;

import actor.LogicActorManager;
import common.LogHelper;
import config.CoupleRoomInfoProvider;
import config.DynamicInfoProvider;
import config.JsonUtil;
import config.bean.FanType;
import data.MoneySubAction;
import database.DBUtil;
import logic.define.GameType;
import logic.majiong.cpstragety.EightyEightStragety;
import logic.majiong.cpstragety.StragetyManager;
import logic.majiong.define.MJPosition;
import logic.majiong.define.MJType;
import logic.record.TaxRecordUtil;
import logic.record.detail.MjDetail;
import logic.record.detail.OnePosDetail;
import net.sf.json.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.Common;
import protobuf.CoupleMajiang;
import protobuf.creator.CommonCreator;
import protobuf.creator.CoupleCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import util.LogUtil;
import util.MiscUtil;
import util.Pair;
import util.Randomizer;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by Administrator on 2016/11/22.
 */
public class CoupleMJDesk extends MJDesk {
	private static final Logger logger = LoggerFactory.getLogger(CoupleMJDesk.class);

	private Map<Integer, CoupleMajiang.PBHupaiRes> hupaiResMap = new HashMap<>();

	public CoupleMJDesk(int deskId,int roomConfId, List<PlayerInfo> playerList) {
		super(deskId,roomConfId, playerList);
		baseScore = CoupleRoomInfoProvider.getInst().getBaseScoreOfRoom(roomConfId);
	}

	private int baseScore;

	@Override
	protected void checkHuapai() {
		player2InfoMap.values().forEach(e -> checkHuapaiForOne(e));
	}

	@Override
	protected void mixAllCard() {
		paiPool = GameUtil.mixAllCard(MJType.COUPLE_MJ);
	}

	@Override
	protected void selectMenfeng() {
		postion2InfoMap.values().forEach(e -> e.setType());
	}

	@Override
	public GameType getGameType() {
		return GameType.COUPLE_MJ;
	}

	@Override
	protected void selectZhuang() {
		int random = Randomizer.nextInt(4);
		if (random < 2) {
			postion2InfoMap.get(MJPosition.EAST).setZhuang(true);
			setCurrentTurn(MJPosition.EAST);
			currOperationTurn = MJPosition.EAST;
		} else {
			postion2InfoMap.get(MJPosition.WEST).setZhuang(true);
			setCurrentTurn(MJPosition.WEST);
			currOperationTurn = MJPosition.WEST;
		}
		if (getRounds() == 1) {
			selectMenfeng();
		}
	}

	@Override
	public int getBaseScore() {
		return this.baseScore;
	}

	@Override
	protected void initMjGameDeskInfo(List<PlayerInfo> playerList) {
		MJPosition position = null;
		for (PlayerInfo player : playerList) {
			PlayerDeskInfo info = new PlayerDeskInfo();
			info.setDesk(this);
			position = (position == null ? MJPosition.EAST : position.nextPosition().nextPosition());
			info.setPosition(position);
			player2InfoMap.put(player, info);
			postion2InfoMap.put(info.getPosition(), info);
//			player.setDesk(this);
		}
	}

	private void checkHuapaiForOne(PlayerDeskInfo info) {
		while (GameUtil.containHuapai(info.getHandCards())) {
			List<Integer> huaPaiList = GameUtil.selectAllHuapai(info.getHandCards());
			for (Integer huapai : huaPaiList) {
				info.addHuaCard(huapai);
			}
			int size = huaPaiList.size();
			for (int i = 0; i < size; i++) {
				info.addHandCard(paiPool.poll());
			}
		}
	}


	public void hupai(PlayerInfo player, List<Integer> chi, List<Integer> ke, List<Integer> jiang) {
		stopAutoChuPaiBeginFuture();
//		logger.info("胡牌的stack,{}",getClassPath());
		if (!isGameing()) {
			return;
		}

		if (!((gameState2 == MajongState2.GUO || gameState2 == MajongState2.HU || gameState2 == MajongState2.BEGIN || gameState2 == MajongState2.MO || gameState2 == MajongState2.CHU || isGangIng))) {
			logger.error("当前状态{}不对，不能胡牌", gameState2);
			return;
		}

        PlayerDeskInfo info = getPlayerDeskInfo(player);
        if (currOperationTurn != info.getPosition()) {
            logger.error("沒輪到當前位置{}胡牌,當前操作位置為{}", info.getPosition(), currOperationTurn);
            return;
        }

		logger.info("位置{} 胡牌", currOperationTurn);

		stopOperationFuture();
		removePositionOperation(info.getPosition());

		//merge self card and the hand card together
		List<Integer> chiList = new ArrayList<>(info.getChiQueue());
		for (Integer value : chi) {
			chiList.add(value);
		}

		List<Integer> keList = new ArrayList<>();
		for (Integer value : ke) {
			keList.add(value + GameConst.AN_MASK);
		}
		keList.addAll(info.getKeQueue());

		logger.info("胡牌  0  吃{} 克 {} 将 {} ，杠{}",JsonUtil.getJsonString(chiList),JsonUtil.getJsonString(keList),JsonUtil.getJsonString(jiang),JsonUtil.getJsonString(info.getGangQueue()));

		List<CoupleFanType> typeList = StragetyManager.getInstance().checkValidFanType(info.getGangQueue(), new LinkedList<>(keList), new LinkedList<>(chiList), jiang);
		if (typeList == null) {
			return;
		}

		logger.info("胡牌  1  翻数为{},类型为 {}",GameUtil.getTotalFanshu(typeList),JsonUtil.getJsonString(typeList));
		// 判斷是否九蓮寶燈
		if (!((info.getChiQueue().size() > 0 || info.getGangQueue().size() > 0 || info.getKeQueue().size() > 0))) {
			List list = new ArrayList<>();
			keList.forEach(e -> {
				list.add(GameUtil.getRealValue(e));
				list.add(GameUtil.getRealValue(e));
				list.add(GameUtil.getRealValue(e));
			});
			chiList.forEach(e->{
				list.add(e);
				list.add(e.intValue()+1);
				list.add(e.intValue()+2);
			});
			jiang.forEach(e->{
				list.add(e);
				list.add(e);
			});
			filter9LianBaodeng(typeList, info.getGangQueue(), info.getChiQueue(), info.getKeQueue(), list);
		}

		filterAllSpecialFan(typeList, player);

		logger.info("胡牌  2  翻数为{},类型为 {}",GameUtil.getTotalFanshu(typeList),JsonUtil.getJsonString(typeList));
		filterBianKanDanDiao(typeList, player, chi, ke, jiang, true);

		logger.info("胡牌  3  翻数为{},类型为 {}",GameUtil.getTotalFanshu(typeList),JsonUtil.getJsonString(typeList));
		if (isSelfTurn(player) && info.isGangIng()) {
			typeList.add(CoupleFanType.EIGHT_3);
		}
		int hut_ype = typeList.contains(CoupleFanType.ONE_9) ? 2 : 1;
		GameUtil.filterIgnoreFanType(typeList);

		int totalFanshu = GameUtil.getTotalFanshu(typeList);
		logger.info("胡牌  4  翻数为{},类型为 {}",GameUtil.getTotalFanshu(typeList),JsonUtil.getJsonString(typeList));

		List<Integer> resultCard = new ArrayList<>();
		GameUtil.addGangToCardList(resultCard, info.getGangQueue());
		GameUtil.addKeToCardList(resultCard, keList);
		GameUtil.addChiToCardList(resultCard, chiList);
		GameUtil.addJiangToCardList(resultCard, jiang);
		List<Integer> handCards = new ArrayList<>();
		GameUtil.addChiToCardList(handCards, chi);
		GameUtil.addKeToCardList(handCards, ke);
		GameUtil.addJiangToCardList(handCards, jiang);
		long totalCoin = getBaseScore() * totalFanshu;
		PlayerInfo oppositePlayer = getNextPlayer(player);
		totalCoin = Math.min(totalCoin, oppositePlayer.getCoin());
		int taxRate = CoupleRoomInfoProvider.getInst().getTaxRate(getConfId());
		long addCoin = totalCoin * (100 - taxRate) / 100;
		long pre_coin = player.getCoin();
		player.updateCoin(addCoin, true);
		player.write(RequestCode.LOG_MONEY.getValue()
				, LogHelper.logGainMoney(player.getPlayerId()
						, MoneySubAction.COUPLE_MJ_GAIN.getValue(), getGameType().getValue(), (int) addCoin, pre_coin, pre_coin + addCoin, player.getIp(), player.getChannel_id(), String.valueOf(player.getPackage_id()), player.getDevice(), getGameId()));
		TaxRecordUtil.sendGamePlayerStatus(player, addCoin);
		detail.addOneRecord(player.getPlayerId(), getPlayerPosition(player), 1, (int) (totalCoin - addCoin), player.getChannel_id(), player.getPackage_id(), player.getDevice(), pre_coin, player.getCoin(), player.getIp());
		List<Pair<Integer, Integer>> coinList = new ArrayList<>();
		coinList.add(new Pair<>(getPlayerPosition(player), (int) addCoin));
		coinList.add(new Pair<>(getPlayerPosition(oppositePlayer), (int) totalCoin * -1));
		pre_coin = oppositePlayer.getCoin();
		oppositePlayer.updateCoin(totalCoin, false);
		oppositePlayer.write(RequestCode.LOG_MONEY.getValue()
				, LogHelper.logLoseMoney(oppositePlayer.getPlayerId()
						, MoneySubAction.COUPLE_MJ_LOSE.getValue(), getGameType().getValue(), (int) totalCoin, pre_coin, pre_coin - totalCoin, oppositePlayer.getIp(), oppositePlayer.getChannel_id(), String.valueOf(oppositePlayer.getPackage_id()), oppositePlayer.getDevice(), getGameId()));
		TaxRecordUtil.sendGamePlayerStatus(oppositePlayer, totalCoin * -1);
		detail.addOneRecord(oppositePlayer.getPlayerId(), getPlayerPosition(oppositePlayer), 0, 0, player.getChannel_id(), player.getPackage_id(), player.getDevice(), pre_coin, oppositePlayer.getCoin(), oppositePlayer.getIp());
		for (PlayerInfo player1 : player2InfoMap.keySet()) {
			if (player1 == player) {
				CoupleMajiang.PBHupaiRes res = CoupleCreator.createPBHupaiRes(getPlayerPosition(player)
						, resultCard, getHuValue(player), typeList, coinList, totalFanshu, getPlayerDeskInfo(oppositePlayer).getHandCards(), String.valueOf(getGameId()));
				hupaiResMap.put(player1.getPlayerId(), res);
				player1.write(ResponseCode.COUPLE_HU, res);
				logger.info("CoupleMJDesk_玩家{}胡牌{}", player1.getPlayerId(),res);
			} else {
				CoupleMajiang.PBHupaiRes res = CoupleCreator.createPBHupaiRes(getPlayerPosition(player), resultCard, getHuValue(player), typeList, coinList, totalFanshu, handCards, String.valueOf(getGameId()));
				hupaiResMap.put(player1.getPlayerId(), res);
				player1.write(ResponseCode.COUPLE_HU, res);
				logger.info("CoupleMJDesk_玩家{}胡牌{}", player1.getPlayerId(),res);
			}
		}
		syncAllPlayerMoney();
		TaxRecordUtil.recordGameTaxInfo(startTime, player2InfoMap.size(), getGameId()
				, getGameType(), getConfId(), getZhuangId(), (int) totalCoin, (int) (totalCoin - addCoin), detail, this);
		record.addCalculateStepCouple(hut_ype, getPlayerPosition(player), getHuValue(player), totalFanshu, GameUtil.fromCoupleFanList(typeList), coinList);
		switchZhuangPlayer(info.getPosition());
		onGameEnd();

		//这样就是能够胡了
	}

	public CoupleMajiang.PBHupaiRes getHuRes(PlayerInfo player) {
		return hupaiResMap.get(player.getPlayerId());
	}

	public void tingpai(PlayerInfo player, CoupleMajiang.PBTingReq request) {
//		if (!((gameState2 == MajongState2.BEGIN||gameState2 == MajongState2.CHI || gameState2 == MajongState2.PENG||gameState2 == MajongState2.MO))) {
//			logger.info("当前状态{}不能听牌", gameState2);
//			return;
//		}
		stopAutoChuPaiBeginFuture();
		logger.info("当前状态{}听牌", gameState2);

		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		List<Pair<Integer, List<Pair<Integer, Integer>>>> result = new ArrayList<>();
		for (CoupleMajiang.PBTingItem tingItem : request.getTingInfoList()) {
			Pair<Integer, List<Pair<Integer, Integer>>> pair = new Pair<>();
			int dropValue = tingItem.getDropCard();
			pair.setLeft(dropValue);
			List<Pair<Integer, Integer>> fanList = new ArrayList<>();
			pair.setRight(fanList);
			result.add(pair);
			List<Integer> handCopy = info.getHandCardsCopy();
			handCopy.remove((Integer) dropValue);
			List<CoupleMajiang.PBHuReq> huList = tingItem.getHuList();
			for (CoupleMajiang.PBHuReq hu : huList) {
				List<Integer> chiList = new ArrayList<>(hu.getChiListList());
				List<Integer> keList = new ArrayList<>(hu.getKeListList());
				List<Integer> jiangList = new ArrayList<>(hu.getJiangValueList());
				List<Integer> allCards = new ArrayList<>();
				GameUtil.addChiToCardList(allCards, chiList);
				GameUtil.addKeToCardList(allCards, keList);
				GameUtil.addJiangToCardList(allCards, jiangList);
				handCopy.forEach(e -> allCards.remove(e));
				if (allCards.size() != 1) {
					logger.debug(" the card count is error {} ", allCards.size());
					continue;
				}
				int huValue = allCards.get(0);
				chiList.addAll(info.getChiQueue());

				for (int i = 0; i < keList.size(); i++) {
					keList.set(i, keList.get(i) + GameConst.AN_MASK);
				}
				keList.addAll(info.getKeQueue());

				logger.info("听牌  0 的值为{} 吃{} 克 {} 将 {} ，杠{} ",huValue,JsonUtil.getJsonString(chiList),JsonUtil.getJsonString(keList),JsonUtil.getJsonString(jiangList),JsonUtil.getJsonString(info.getGangQueue()));

				List<CoupleFanType> typeList = StragetyManager.getInstance().checkValidFanTypeTing(info.getGangQueue(), new LinkedList<>(keList), new LinkedList<>(chiList), jiangList);
				if (typeList == null) {
					return;
				}
				logger.info("听牌 1 的值为{} 翻数{},数据为{}",huValue, GameUtil.getTotalFanshu(typeList), JsonUtil.getJsonString(typeList));

                // 判斷是否九蓮寶燈
				if (!((info.getChiQueue().size() > 0 || info.getGangQueue().size() > 0 || info.getKeQueue().size() > 0))) {
					List list = new ArrayList<>();
					keList.forEach(e -> {
						list.add(GameUtil.getRealValue(e));
						list.add(GameUtil.getRealValue(e));
						list.add(GameUtil.getRealValue(e));
					});
					chiList.forEach(e->{
						list.add(e);
						list.add(e.intValue()+1);
						list.add(e.intValue()+2);
					});
					jiangList.forEach(e->{
						list.add(e);
						list.add(e);
					});
					filter9LianBaodeng(typeList, info.getGangQueue(), info.getChiQueue(), info.getKeQueue(), list);
				}

				filterAboutTingpai(typeList, player);
				typeList.add(CoupleFanType.TWO_10);
				if ((info.isZhuang() && info.neverHandCard()) || (!info.isZhuang() && info.getAddHandTimes() == 1)) {
					typeList.add(CoupleFanType.THIRTY_TWO_4);
				}
				
//				filterHuapaiFanshu(typeList, player);	// 去掉花牌
				filterMenfengQuanfeng(typeList, player);

				logger.info("听牌 2 的值为{} 翻数{},数据为{}",huValue, GameUtil.getTotalFanshu(typeList), JsonUtil.getJsonString(typeList));

				filterBianKanDanDiao(typeList, player, chiList, keList, jiangList, false);

				logger.info("听牌 3 的值为{} 翻数{},数据为{}",huValue, GameUtil.getTotalFanshu(typeList), JsonUtil.getJsonString(typeList));

				GameUtil.filterIgnoreFanType(typeList);

                int totalFanshu = GameUtil.getTotalFanshu(typeList);

				logger.info("听牌 4 的值为{} 翻数{},数据为{}",huValue, totalFanshu, JsonUtil.getJsonString(typeList));
				fanList.add(new Pair<>(huValue, totalFanshu));
			}
		}
		info.addTingResult(result);
		info.setHuCache(request);
		CoupleMajiang.PBTingFanRes builder = CoupleCreator.createPBTingFanRes(result);
		player.write(ResponseCode.COUPLE_ON_TING, builder);
	}

	@Override
	public MJPosition nextPosition() {
		return currentTurn.nextPosition().nextPosition();
	}

	@Override
	public void chiPai(PlayerInfo player, int type) {
		if (!(gameState2 == MajongState2.CHU ||gameState2 == MajongState2.GUO)) {
			logger.error("状态{}不对,不能吃牌", gameState2);
			return;
		}

		ChiType chiType = ChiType.getByValue(type);
		if (chiType == null) {
			return;
		}
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (!GameUtil.checkChi(info, getCurrentTurnInfo().getDeskPaiStack().peek(), chiType)) {
			return;
		}

        if (currOperationTurn != info.getPosition()) {
            logger.error("沒輪到該位置{}吃牌", info.getPosition());
            return;
        }

        List<Integer> ops = operationList.get(currOperationTurn);

        if (ops == null || !ops.contains(MajiangOperationType.CHI.getValue())) {
            logger.error("該位置{}沒有操作列表,不能吃", currOperationTurn);
            return;
        }

        setGangFalse();
		stopOperationFuture();
        removePositionOperation(info.getPosition());

		int chiValue = getCurrentTurnInfo().getDeskPaiStack().pop();
		switch (chiType) {
			case LEFT:
				info.addChiLeft(chiValue);
				msgHelper.notifyChi(player, chiValue, chiValue);
				break;
			case RIGHT:
				info.addChiRight(chiValue);
				msgHelper.notifyChi(player, chiValue - 2, chiValue);
				break;
			case MIDDLE:
				info.addChiMiddle(chiValue);
				msgHelper.notifyChi(player, chiValue - 1, chiValue);
				break;
			default:
				break;
		}
		info.addHandTimes();
		setCurrentTurn(getPlayerPositionType(player));

		LogUtil.logLogger(logger, "吃牌", "" + getGameId(), "" + getDeskId(), "" + getGameType(), "" + player.getPlayerId(), "" + chiType, "" + chiValue);

		currentTurn = currOperationTurn;

		setGameState2(MajongState2.CHI);
		thinkAftChi();
	}

	@Override
	public PlayerDeskInfo getNexPlayerDeskInfo(PlayerInfo player) {
		PlayerDeskInfo info = player2InfoMap.get(player);
		if (info == null) {
			return null;
		}
		return postion2InfoMap.get(info.getPosition().nextPosition().nextPosition());
	}
	
	@Override
	public void sortHandCard(PlayerInfo player) {
		PlayerDeskInfo info = player2InfoMap.get(player);
		info.getHandCards().sort((h1, h2) -> {
			return h1.intValue() > h2.intValue() ? 1 : -1;
		});
	}


	//判断是否边张, 坎张, 单将
	private void filterBianKanDanDiao(List<CoupleFanType> typeList, PlayerInfo player, List<Integer> chi, List<Integer> ke, List<Integer> remain, boolean isHu) {
		int huValue = getHuValue(player);
//		for (Integer value : remain) {
//			if (value == huValue && getPlayerDeskInfo(player).getTotalCardCotainGangChike() != 14) {
//				typeList.add(CoupleFanType.ONE_8);
//				return;
//			}
//		}
		if (getPlayerDeskInfo(player).getHandCards().size() == 1 && remain.get(0).intValue() == huValue) {
			typeList.add(CoupleFanType.ONE_8);
			return;
		}

		if ((chi.contains(GameConst.WAN_1) && huValue == GameConst.WAN_3)
				|| (chi.contains(GameConst.WAN_7) && huValue == GameConst.WAN_7)) {
			if (isHu)   // 胡牌才算
				typeList.add(CoupleFanType.ONE_6);
		}
		for (Integer value : chi) {
			if (!GameUtil.isWanCard(value)) {
				continue;
			}
			if (huValue == value + 1) {
				if (isHu)  // 胡牌才算
					typeList.add(CoupleFanType.ONE_7);
				break;
			}
		}
	}

	private void filterAllSpecialFan(List<CoupleFanType> typeList, PlayerInfo player) {
		filterZimo(typeList, player);
		filterAboutTingpai(typeList, player);
		filterTiandiRenhu(typeList, player);
		filterHuapaiFanshu(typeList, player);
		filterQiangGangHe(typeList, player);
		filterBuqiuQuanqiu(typeList, player);
		filterLastCardHu(typeList, player);
		filterTheLastOne(typeList, player);
		filterMenfengQuanfeng(typeList, player);
	}

	// 九蓮寶燈
	private void filter9LianBaodeng(List<CoupleFanType> typeList, Stack<Integer> gang, Queue<Integer> ke, Queue<Integer> chi, List<Integer> remain) {
		if (EightyEightStragety.three(gang, chi, ke, remain)) {
			typeList.add(CoupleFanType.EIGHTY_EIGHT_3);
		}
	}

	private void filterMenfengQuanfeng(List<CoupleFanType> typeList, PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		if (info.getType() == MenFengType.DONG) {
			for (Integer value : info.getKeQueue()) {
				if (GameUtil.getRealValue(value) == GameConst.DONG_FENG) {
					typeList.add(CoupleFanType.TWO_1);
				}
			}
		} else {
			for (Integer value : info.getKeQueue()) {
				if (GameUtil.getRealValue(value) == GameConst.XI_FENG) {
					typeList.add(CoupleFanType.TWO_1);
				}
			}
		}
	}

	//判断花牌的番数
	private void filterHuapaiFanshu(List<CoupleFanType> typeList, PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		int huaSize = info.getHuaCards().size();
		if (huaSize >= 8) {
			typeList.add(CoupleFanType.SIXTEEN_3);
		} else if (huaSize == 0) {
			typeList.add(CoupleFanType.FOUR_6);
		} else {
			for (int i = 0; i < huaSize; i++) {
				typeList.add(CoupleFanType.ONE_4);
			}
		}
	}

	//判断是否报听是否天听
	private void filterAboutTingpai(List<CoupleFanType> typeList, PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		if (info.isBaoTing()) {
			typeList.add(CoupleFanType.TWO_10);
			if (info.getBaotingDiscardTimes() == 0) {
				typeList.add(CoupleFanType.FOUR_5);
			}
		}
		if (info.isTianting()) {
			typeList.add(CoupleFanType.THIRTY_TWO_4);
		}
	}

	//判断是否和绝张
	private void filterTheLastOne(List<CoupleFanType> typeList, PlayerInfo player) {
		List<Integer> availableCards = new ArrayList<>();
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		GameUtil.addChiToCardList(availableCards, info.getChiQueue());
		GameUtil.addKeToCardList(availableCards, info.getKeQueue());
		GameUtil.addGangToCardList(availableCards, info.getGangQueue());
		availableCards.addAll(info.getHandCards());
		player2InfoMap.values().forEach(e -> availableCards.addAll(e.getDeskPaiStack()));
		availableCards.addAll(info.getDeskPaiStack());
		if (GameUtil.getCountOfValue(availableCards, getHuValue(player)) == 4) {
			typeList.add(CoupleFanType.FOUR_4);
		}
	}

	//判断是否海底捞月,妙手回春
	private void filterLastCardHu(List<CoupleFanType> typeList, PlayerInfo player) {
		if (isSelfTurn(player)) {
			if (paiPool.size() == 0) {
				typeList.add(CoupleFanType.EIGHT_1);
			}
		} else {
			if (paiPool.size() == 1) {
				typeList.add(CoupleFanType.EIGHT_2);
			}
		}
	}

	//判断是否是不求人, 全求人,门前清
	private void filterBuqiuQuanqiu(List<CoupleFanType> typeList, PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info.getTotalCardCotainGangChike() == 14) {
			if (info.getGangQueue().size() == 0 && info.getKeQueue().size() == 0 && info.getChiQueue().size() == 0) {
				typeList.add(CoupleFanType.FOUR_3);
			}
		} else {
			int qiurenCount = info.getQiurenCount();
			if (qiurenCount == 4) {
				typeList.add(CoupleFanType.SIX_6);
			}
			if (qiurenCount == 0) {
				typeList.add(CoupleFanType.TWO_9);
			}
		}
	}

	//判断是否抢杠和
	private void filterQiangGangHe(List<CoupleFanType> typeList, PlayerInfo player) {
		PlayerDeskInfo info = getCurrentTurnInfo();
		PlayerDeskInfo selfInfo = getPlayerDeskInfo(player);
		if (info == selfInfo) {
			return;
		}
		if (info.isGangIng() && isGangIng) {
			typeList.add(CoupleFanType.EIGHT_4);
		}
	}

	//判断是否是天地人和
	private void filterTiandiRenhu(List<CoupleFanType> typeList, PlayerInfo player) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		if (isZhuangjia(player)) {
			if (info.neverHandCard() && isSelfTurn(player)) {
				typeList.add(CoupleFanType.EIGHTY_EIGHT_8);
			}
		} else {
			if (info.neverHandCard()) {
				typeList.add(CoupleFanType.SIXTY_FOUR_6);
			}
			if (info.getAddHandTimes() == 1 && isSelfTurn(player)) {
				typeList.add(CoupleFanType.EIGHTY_EIGHT_9);
			}
		}
	}

	//判断是否自摸
	private void filterZimo(List<CoupleFanType> typeList, PlayerInfo player) {
		if (getPlayerDeskInfo(player).getTotalCardCotainGangChike() == 14) {
			typeList.add(CoupleFanType.ONE_9);
		}
	}

	@Override
	protected void beforeGame() {
//        checkTianTing();
		stopOperationFuture();
		PlayerInfo p = getPlayerByPosition(currentTurn);
		opBeginTime = MiscUtil.getCurrentSeconds();
		// 设置状态为出牌
		gameState2 = MajongState2.MO;
		sortHandCard(p);
		autoChuPaiBeginFuture = LogicActorManager.registerOneTimeTask(time12, () -> discardCard(p, getPlayerDeskInfo(p).getLastHandCard()), getDeskId());
	}

    /**
     * 检查是否天听
     */
	private void checkTianTing(){
        player2InfoMap.values().forEach(e -> checkTianTing(e));
    }

	private void checkTianTing(PlayerDeskInfo info) {
		if (info.isZhuang()) {
			if (info.neverHandCard() && MajongRule.checkCoupleTingpai(info.getHandCards(), true)) {
				info.setTianting(true);
			}
		} else {
			if (info.getAddHandTimes() == 1 && MajongRule.checkCoupleTingpai(info.getHandCards(), true)) {
				info.setTianting(true);
			}
		}
	}

	@Override
	protected void clearDeskInfo() {
		hupaiResMap.clear();
	}

	@Override
	protected void calculateGang(PlayerInfo player, int value) {

	}

	@Override
	protected boolean canChi() {
		return true;
	}

	@Override
	protected void onAllCardOver(boolean disband) {
		for (PlayerInfo player : player2InfoMap.keySet()) {
			Common.PBInt32List builder = CommonCreator.createPBInt32List(getNexPlayerDeskInfo(player).getHandCards());
			player.write(ResponseCode.COUPLE_LIUJU,  builder);

			LogUtil.logLogger(logger, "流局", builder, "" + getGameId(), "" + getDeskId(), "" + getGameType(), "" + player.getPlayerId());
		}
		onGameEnd();
		switchZhuangPlayer(getZhuangInfo().getPosition().nextPosition().nextPosition());
	}

	@Override
	protected MajongType getIgnoreType(MJPosition position) {
		return null;
	}

	@Override
	protected boolean canDropCard(PlayerInfo player, int card) {
		if (GameUtil.isHuapai(card)) {
			logger.debug("the hua pai can't be dropped");
			return false;
		}
		return true;
	}

	@Override
	protected int getEnterTimes() {
		return 0;
	}

	@Override
	public void doTing(PlayerInfo player, int value) {
		PlayerDeskInfo info = getPlayerDeskInfo(player);
		if (info == null) {
			return;
		}
		if (!info.dropCanTing(value)) {
			return;
		}

        if (info.isBaoTing()) {
            return;
        }
        if (gameState2 == MajongState2.CHU){ // 定时器已经出牌了 
        	return;
        }
        info.setBaoTing(true);
		info.doTing(value);
		msgHelper.notifyTing(player);
		discardCard(player, value);
	}

	@Override
    protected void dealOneCard(MJPosition position) {
		if(isDestroyed.get()){
			logger.error("桌子{},{}已经销毁{}",getGameType(),getConfId(),new Exception().getStackTrace()[0]);
    		destroy();
    		return;
    	}
        logger.info("位置{}处理牌",position);
		// 杠牌,胡牌，出牌 之后可以摸牌
		if (!((gameState2 == MajongState2.GUO || gameState2 == MajongState2.CHU || gameState2 == MajongState2.GANG || gameState2 == MajongState2.HU))) {
			logger.error("当前状态 {} 不对,不能摸牌", gameState2);
			return;
		}

        if (currentTurn != position) {
            logger.info("沒有輪到該位置{}摸牌，當前操作位置為{}", position, currentTurn);
            return;
        }

		setCurrentTurn(position);
		currOperationTurn = position;
		if (paiPool.size() == 0) {
			onAllCardOver(false);
			return;
		}
		// 摸牌后不思考
		operationList.clear();
		PlayerDeskInfo info = postion2InfoMap.get(position);
		if (info.getTotalCardCotainGangChike() == 14) {
			return;
		}
		List<Integer> allCards = new ArrayList<>();
		int huaNum = 0;
		while (GameUtil.isHuapai(paiPool.peek())) {
			int card = paiPool.poll();
			allCards.add(card);
			huaNum++;
			info.addHuaCard(card);
			if (paiPool.size() <= 0) {
				break;
			}
		}
		if (paiPool.size() == 0) {
			msgHelper.notifyMoPai(position, allCards, 0);
			onAllCardOver(false);
		} else {
			int pai = paiPool.poll();
			allCards.add(pai);
			info.addHandCard(pai);
			info.addHandTimes();
			msgHelper.notifyMoPai(position, allCards, pai);
		}
		setGameState2(MajongState2.MO);

		thinkAftMo(huaNum);
	}

	@Override
	public boolean dealHuInfo() {
		return false;
	}
}
