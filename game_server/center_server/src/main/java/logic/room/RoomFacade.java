package logic.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chr.Player;
import config.CoupleRoomInfoProvider;
import config.bean.CoupleRoom;
import define.AppId;
import define.GameType;
import define.constant.LoginStatusConst;
import io.netty.channel.ChannelHandlerContext;
import logic.desk.DeskInfo;
import logic.desk.DeskManager;
import logic.desk.GroupDeskManager;
import network.ServerManager;
import network.ServerSession;
import packet.CocoPacket;
import proto.Common;
import proto.CoupleMajiang;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import util.Pair;

/**
 * Created by Administrator on 2016/12/12.
 */
public abstract class RoomFacade implements GameRoomInterface {

	private static final Logger logger = LoggerFactory.getLogger(RoomFacade.class);

	protected Map<Integer, Queue<Player>> enterPlayerMap = new HashMap<>();

//	protected Map<Integer, Desk> deskMap = new ConcurrentHashMap<>();

	/**
	 * 私房退出桌子 同意解散 一天到期 后台删除桌子
	 * 匹配退出桌子 自己游戏未进行时退出 所有人退出 后台删除桌子
	 */
	protected Map<Integer, Set<Integer>> roomToPlayers = new HashMap<>();

	@Override
	public void enterGame(Player player) {

	}

	protected boolean checkMoneyValid(Player player, CoupleRoom roomConf) {
		if (player.getCoin() < roomConf.getMinReq()) {
			return false;
		}
		if (roomConf.getMaxReq() != -1 && player.getCoin() > roomConf.getMaxReq()) {
			return false;
		}
		return true;
	}

	protected abstract int getGameNeedPeople();

	protected abstract GameType getGameType();

	@Override
	public void enterGameRoom(Player player, int roomId) {
		CoupleRoom room = CoupleRoomInfoProvider.getInst().getRoomConf(roomId);
		if (room == null) {
			logger.warn("player want to enter a room that doesn't exist");
			return;
		}
		if (!checkMoneyValid(player, room)) {
			logger.warn(" player money not enough");
			return;
		}
		switch (room.getMode()) {
			case 4:
				enterNiuNiuRoom(player, roomId);
				break;
			case 6:
			case 9:
			case 10:
				doDeskGroupEnterMatchingQueue(player, roomId);
				break;
			default:
				doEnterMatchingQueue(player, roomId);
				break;
		}
	}

	protected abstract void enterNiuNiuRoom(Player player, int roomId);

	private void doEnterMatchingQueue(Player player, int roomId) {
//		player.setRoomId(roomId);
		player.setMatchingGameId(getGameType().getValue());
		player.setMatchingRoomId(roomId);
		player.write(ResponseCode.LOBBY_MACHING, CommonCreator.createPBPair(player.getMatchingGameId(), roomId));
		Queue<Player> enterPlayerQueue = enterPlayerMap.get(roomId);
		if (enterPlayerQueue == null) {
			enterPlayerQueue = new LinkedList<>();
			enterPlayerMap.put(roomId, enterPlayerQueue);
		}
		if (!enterPlayerQueue.contains(player)) {
			enterPlayerQueue.add(player);
			int roomNum = addRoomPlayerNum(roomId, player.getPlayerId());
			logger.info("玩家{}成功加入{}匹配队列,当前匹配人数{},房间人数{}", player.getPlayerId(), roomId, enterPlayerQueue.size(), roomNum);
			onPlayerEnterQueue(roomId);
		}
	}

//	private void doZJhEnterMatchingQueue(Player player, int roomId) {
////		player.setRoomId(roomId);
//		player.setMatchingGameId(getGameType().getValue());
//		player.setMatchingRoomId(roomId);
//		player.write(ResponseCode.LOBBY_MACHING, CommonCreator.createPBPair(player.getMatchingGameId(), roomId));
//		addRoomPlayerNum(roomId,player.getPlayerId());
//		ZjhDeskManager.getIns().addPlayer(player,roomId);
//	}

	private void doDeskGroupEnterMatchingQueue(Player player, int roomId) {
//		player.setRoomId(roomId);
		player.setMatchingGameId(getGameType().getValue());
		player.setMatchingRoomId(roomId);
		player.write(ResponseCode.LOBBY_MACHING, CommonCreator.createPBPair(player.getMatchingGameId(), roomId));
		addRoomPlayerNum(roomId, player.getPlayerId());
		GroupDeskManager.getIns().matchingDesk(player, getGameType(), roomId);
	}

	@Override
	public void onPlayerEnterQueue(int roomId) {

	}


	//队列人数满足条件啊开始进行匹配
	private void dispatchQueue(int roomId, List<Player> playerList) {
		while (true) {
			if (!selectPlayerToGame(roomId, playerList)) {
				break;
			}
		}
		enterPlayerMap.get(roomId).addAll(playerList);                    //重新放回匹配队列
	}

	//是否能找到玩家进行匹配
	private boolean selectPlayerToGame(int roomId, List<Player> playerList) {
		if (playerList.size() < getGameNeedPeople()) {
			return false;
		}
		List<Player> result = tryMatchPlayerList(playerList, "ios", getGameNeedPeople());                    //开始向logic server请求 创建玩家房间的信息
		if (result == null) {
			return false;
		}
		playerList.removeAll(result);
		doCreateGame(roomId, result);
		return true;
	}

	private void doCreateGame(int roomId, List<Player> playerList) {
		ServerSession serverSession = ServerManager.getInst().getNoStopMinLoadSession(AppId.LOGIC);
		ChannelHandlerContext ioSession = serverSession.getIoSession();//如果serverSession则报错返回，避免数据问题
		DeskInfo deskInfo = DeskManager.getInst().getNorPrivateDesk(null);
		deskInfo.setGameId(getGameType().getValue());
		deskInfo.setRoomId(roomId);
		deskInfo.setIoSession(ioSession);
		deskInfo.setPlayerList(playerList);

		playerList.forEach(e -> {
			e.addMatchPlayers(playerList);
			e.updateLoginStatus(roomId + LoginStatusConst.AT_GAME_ROOM_ING);        /// 用加100的方式来判断不同的场景
			e.setDeskInfo(deskInfo);
//			e.setRoomId(roomId);
			logger.info("玩家{}匹配创建桌子{},{}", e.getPlayerId(), roomId, deskInfo.getDeskId());
		});

		CocoPacket packet = new CocoPacket(RequestCode.LOGIC_CREATE_DESK, createPBCreateDesk(getGameType(), roomId, deskInfo.getDeskId(), playerList));
		serverSession.sendRequest(packet);
		logger.debug(" send request to logic create desk");
	}

	private List<Player> tryMatchPlayerList(List<Player> playerList, String device, int needPeople) {
		List<Player> result = new ArrayList<>();
		List<String> useIps = new ArrayList<>();
		//任何条件都不放开的赛选
		if (tryBestQueueMethod(result, useIps, playerList, device, true, true, true, true, true, needPeople)) {
			return result;
		}
		//放开对付费的需求
		if (tryBestQueueMethod(result, useIps, playerList, device, true, true, true, true, false, needPeople)) {
			return result;
		}
		//放开对老用户的需求
		if (tryBestQueueMethod(result, useIps, playerList, device, true, true, true, false, false, needPeople)) {
			return result;
		}
		//放开对设备的需求
		if (tryBestQueueMethod(result, useIps, playerList, device, true, true, false, false, false, needPeople)) {
			return result;
		}
		//放开对只匹配一次的需求
		if (tryBestQueueMethod(result, useIps, playerList, device, true, false, false, false, false, needPeople)) {
			return result;
		}
		/* never give up the request for ip
		if (tryBestQueueMethod(result, useIps, playerList, device, false, false, false, false, false)) {
			return result;
		}
		*/
		return null;
	}

	private boolean tryBestQueueMethod(List<Player> result, List<String> useIps, List<Player> allPlayer, String device
			, boolean needIp, boolean needOne, boolean needDevice, boolean needOld, boolean needPay, int needPeople) {

		return false;
	}

	private boolean isPlayerMatch(List<Player> playerList, Player player) {
		for (Player p : playerList) {
			if (p.isAlreadyMatchPlayer(player)) {
				return true;
			}
		}
		return false;
	}

	public CoupleMajiang.PBCreateDesk createPBCreateDesk(GameType game, int roomId, int deskId, List<Player> playerList) {
		CoupleMajiang.PBCreateDesk.Builder builder = CoupleMajiang.PBCreateDesk.newBuilder();
		builder.setGameType(game.getValue());
		builder.setRoomId(roomId);
		builder.setDeskId(deskId);
		Common.PBPlayerInfoList.Builder playerBuilder = Common.PBPlayerInfoList.newBuilder();
		CoupleRoom room = CoupleRoomInfoProvider.getInst().getRoomConf(roomId);
		playerList.forEach(e -> {
			Common.PBPlayerInfo.Builder info = Common.PBPlayerInfo.newBuilder();
			info.setPlayerId(e.getPlayerId());
			if (2 == room.getClassify()) {                                                // tiyan room and dont't save this game record
				info.setCoin(e.getTiyanCoin());
			} else {
				info.setCoin((int) e.getCoin());
			}
			info.setName(e.getName());
			info.setIcon(e.getIcon());
			info.setGender(e.getGender().getValue());
			info.setPosition(1);
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

	@Override
	public void onPlayerLogout(Player player) {

	}

	@Override
	public void removeDesk(int deskId) {
	}


	@Override
	public void leaveGame(Player player) {

	}

	@Override
	public void leaveGameRoom(Player player) {
		if (enterPlayerMap != null) {
			int matchingRoomId = player.getMatchingRoomId();
			Queue<Player> enterPlayerQueue = enterPlayerMap.get(matchingRoomId);
			if (enterPlayerQueue != null) {
				enterPlayerQueue.remove(player);
				logger.info("玩家{}离开{}匹配队列,当前匹配队列人数{}", player.getPlayerId(), matchingRoomId, enterPlayerQueue.size());
			}
		}
		int roomId = player.getDeskOrMatchRoomId();
		if (roomId != 0) {
			int roomNum = reduceRoomPlayerNum(roomId, player.getPlayerId());
			player.setMatchingGameId(0);
			player.setMatchingRoomId(0);
			player.updateLoginStatus(LoginStatusConst.ENTER_HALL);
			logger.info("玩家{}离开{}房间,当前房间人数{}", player.getPlayerId(), roomId, roomNum);
		}
//		player.setRoomId(0);
	}

	@Override
	public int getPlayerCount() {
		int total = 0;
		for (Set<Integer> value : roomToPlayers.values()) {
			total += value.size();
		}
		return total;
	}

	public List<Pair<Integer, Integer>> getRoomPlayerList() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		roomToPlayers.forEach((e, f) -> result.add(new Pair<>(e, f.size())));
		return result;
	}

	public List<Pair<Integer, Integer>> getRoomPlayerList_ex() {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		roomToPlayers.forEach((e, f) -> {
			if (0 != CoupleRoomInfoProvider.getInst().getRoomStatus(e)) {
				result.add(new Pair<>(e, f.size()));
			}
		});
		return result;
	}

	public List<Pair<Integer, Integer>> getRoomStatusList() {

		List<Pair<Integer, Integer>> result = new ArrayList<>();
		roomToPlayers.forEach((e, f) -> {
			result.add(new Pair<>(e, CoupleRoomInfoProvider.getInst().getRoomStatus(e)));
		});

		return result;
	}

	public int addRoomPlayerNum(int roomId, int playerId) {
		Set<Integer> currentValue = roomToPlayers.get(roomId);
		if (currentValue == null) {
			currentValue = new HashSet<>();
			roomToPlayers.put(roomId, currentValue);
		}
		currentValue.add(playerId);
		return currentValue.size();
	}

	public int reduceRoomPlayerNum(int roomId, int playerId) {
		Set<Integer> currentValue = roomToPlayers.get(roomId);
		if (currentValue == null) {
			return 0;
		}
		currentValue.remove(playerId);
		return currentValue.size();
	}

	@Override
	public void initGameRoom() {
		for (CoupleRoom conf : CoupleRoomInfoProvider.getInst().getCoupleRoomCfgMap(getGameType()).values()) {
			if (conf == null) {
				continue;
			}
			roomToPlayers.put(conf.getId(), new HashSet<>());
		}
	}

	@Override
	public void addGameRoom() {
		for (CoupleRoom conf : CoupleRoomInfoProvider.getInst().getCoupleRoomCfgMap(getGameType()).values()) {
			if (conf == null) {
				continue;
			}
			if (!roomToPlayers.containsKey(conf.getId())) {
				roomToPlayers.put(conf.getId(), new HashSet<>());
			}
		}
	}

	public Set<Integer> getPlayerIds(int roomId) {
		return roomToPlayers.get(roomId);
	}


	/**
	 * 判断是否在房间中
	 *
	 * @param roomId
	 * @param playerId
	 * @return
	 */
	public boolean isInRoom(int roomId, int playerId) {
		Set<Integer> set = getPlayerIds(roomId);
		return set != null && set.contains(playerId);
	}
}
