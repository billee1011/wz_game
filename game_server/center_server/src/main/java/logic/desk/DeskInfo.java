package logic.desk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import chr.Player;
import define.AppId;
import define.GameType;
import define.Position;
import define.constant.LoginStatusConst;
import io.netty.channel.ChannelHandlerContext;
import logic.room.LobbyGameManager;
import network.ServerManager;
import network.ServerSession;
import packet.CocoPacket;
import proto.Common;
import proto.CoupleMajiang;
import proto.Lobby;
import proto.Xueniu;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import util.MiscUtil;
import util.NettyUtil;
import util.Randomizer;

/**
 * Created by Administrator on 2017/2/7.
 */
public class DeskInfo implements Serializable{
	public static final int INIT_DISBAND_SECOND = 0;
	public static final int ZJH_NEED_PLAYER_COUNT = 6;
	public static final int SCMJ_NEED_PLAYER_COUNT = 4;
	private List<Player> playerList;
	private int deskId;
	private int sessionId;
	private Lobby.PBCreateDeskReq req;
	private int gameId;
	private int roomId;
	private Map<Integer, Position> player2Position;
	private List<Position> positionList = null;
	private boolean gaming;
	protected Player creator;
	private int disbandTime;
	private List<Player> playerVoteList = new ArrayList<>();
	private transient ScheduledFuture<?> disbandFuture = null;
	private static final int DISBAND_SECONDS = 5 * 60;

	public DeskInfo(int deskId) {
		this.deskId = deskId;
		this.gaming = false;
	}

	public int getSize() {
		return playerList.size();
	}
	
	public String getGameName(){
		GameType gameType = GameType.getByValue(getGameId());
		return gameType != null ? gameType.name() : "未知";
	}

	public void addNoPosPlayer(Player player) {
		if (playerList == null) {
			playerList = new ArrayList<>();
		}
		playerList.add(player);
		player.setDeskInfo(this);
	}
	
	public void addPosPlayer(Player player) {
		if (playerList == null) {
			playerList = new ArrayList<>();
		}
		playerList.add(player);
		if (player2Position == null) {
			player2Position = new HashMap<>();
		}
		player2Position.put(player.getPlayerId(), randomPosition());
//		player.setRoomId(getDeskId());
		player.setDeskInfo(this);
	}
	
	public int getPosition(Player player){
		return player2Position.get(player.getPlayerId()).getValue();
	}
	
	public void setCreator(Player p){
		this.creator = p;
	}

	public Xueniu.PBDisbandRes createPBDisbandInfo() {
		Xueniu.PBDisbandRes.Builder builder = Xueniu.PBDisbandRes.newBuilder();
		if (disbandTime == INIT_DISBAND_SECOND) {
			builder.setDisbanding(false);
		} else {
			builder.setDisbanding(true);
			Xueniu.PBDisbandInfo.Builder subBuilder = Xueniu.PBDisbandInfo.newBuilder();
			subBuilder.setDisbandTime(disbandTime);
			playerVoteList.forEach(e -> subBuilder.addAgreeList(player2Position.get(e.getPlayerId()).getValue()));
			builder.setInfo(subBuilder);
		}
		return builder.build();
	}

	public boolean isGaming() {
		return gaming;
	}

	public void setGaming(boolean gaming) {
		this.gaming = gaming;
	}

	public void setReq(Lobby.PBCreateDeskReq req) {
		this.req = req;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	public int getRoomId() {
		if(isPrivateRoom()){
			return getDeskId();
		}
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getDeskId() {
		return deskId;
	}

	public void setDeskId(int deskId) {
		this.deskId = deskId;
	}

	public List<Player> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<Player> playerList) {
		this.playerList = playerList;
	}

	public void setIoSession(ChannelHandlerContext ioSession) {
		if (ioSession == null) {
			return;
		}
		ServerSession serverSession = NettyUtil.getAttribute(ioSession, ServerSession.KEY);
		if (serverSession == null) {
			return;
		}
		this.sessionId = serverSession.getServerId();
		
		if(getGameId() == GameType.NIUNIU.getValue()){
    		serverSession.addLoadFactor(ServerSession.NIU_NIU_LOAD_FACTOR);//让这个服务器变大，因为百人牛牛无人数限制
    	}else{
    		serverSession.addLoadFactor();
    	}
	}

	public boolean isFull() {
		GameType gameType = GameType.getByValue(getGameId());
		switch (gameType) {
			case XUENIU: 
				return playerList.size() >= SCMJ_NEED_PLAYER_COUNT;
			case XUEZHAN: 
				return playerList.size() >= SCMJ_NEED_PLAYER_COUNT;
			case ZJH:     //扎金花
				return playerList.size() >= ZJH_NEED_PLAYER_COUNT;
			case CLASS_NIU:
				return playerList.size() >= 5;
			case GRAD_NIU:
				return playerList.size() >= 5;
			default:
				return true;
		}
	}
	
	/** 游戏最少需要人数是否满足 */
	public boolean canCreateDesk(){
		GameType gameType = GameType.getByValue(getGameId());
		switch (gameType) {
			case XUENIU: 
				return playerList.size() == SCMJ_NEED_PLAYER_COUNT;
			case XUEZHAN: 
				return playerList.size() == SCMJ_NEED_PLAYER_COUNT;
			case ZJH:     //扎金花
				return playerList.size() >= 2;
			case CLASS_NIU:
				return playerList.size() >= 2;
			case GRAD_NIU:
				return playerList.size() >= 2;
			default:
				return false;
		}
	}

	public void playerWantDisband(Player player) {
		cancelDisbandFuture();
		disbandTime = MiscUtil.getCurrentSeconds() + DISBAND_SECONDS;
		disbandFuture = CenterActorManager.registerOneTimeTaskDeskThread(DISBAND_SECONDS * 1000, () -> {
			playerVoteList.clear();
			writeToLogic(new CocoPacket(RequestCode.LOGIC_DISBAND_ROOM_DESK, CommonCreator.createPBInt32(getDeskId())));
		});
		notifyMessage(ResponseCode.LOBBY_PLAYER_WANT_DISBAND, CommonCreator.createPBInt32(player.getPlayerId()));
	}
	
	public void destroy() {
		cancelDisbandFuture();
		ServerSession serverSession = getBindServerSession();
        if (serverSession != null) {
        	if(getGameId() == GameType.NIUNIU.getValue()){
        		serverSession.reduceFactor(ServerSession.NIU_NIU_LOAD_FACTOR);
        	}else{
        		serverSession.reduceFactor();
        	}
        }
	}

	public void cancelDisbandFuture() {
		if (disbandFuture != null) {
			disbandFuture.cancel(true);
			disbandFuture = null;
		}
	}

	public void playerRefuseDisband(Player player) {
		playerVoteList.clear();
		cancelDisbandFuture();
		disbandTime = 0;
		notifyMessage(ResponseCode.LOBBY_DISBAND_FAILED, CommonCreator.createPBInt32(player.getPlayerId()));
	}

	public void playerAgreeDisband(Player player) {
		playerVoteList.add(player);
		notifyMessage(ResponseCode.LOBBY_PLAYER_AGREE_DISBAND, CommonCreator.createPBInt32(player.getPlayerId()));
		if (allAgreeDisband()) {
			playerVoteList.clear();
			writeToLogic(new CocoPacket(RequestCode.LOGIC_DISBAND_ROOM_DESK, CommonCreator.createPBInt32(getDeskId())));
		}
	}

	public boolean allAgreeDisband() {
		if (playerVoteList.size() == playerList.size()) {
			return true;
		}
		return false;
	}

	private ChannelHandlerContext getSession() {
		ServerSession serverSession = ServerManager.getInst().getServerSession(AppId.LOGIC, sessionId);
		if (serverSession == null) {
			return null;
		}
		return serverSession.getIoSession();
	}

	public ServerSession getBindServerSession() {
		ChannelHandlerContext ctx = getSession();
		if (ctx == null) {
			return null;
		}
		return NettyUtil.getAttribute(ctx, ServerSession.KEY);
	}


	public void writeToLogic(CocoPacket packet) {
		ChannelHandlerContext ioSession = getSession();
		if (ioSession == null) {
			return;
		}
		packet.resetReqIdToRequest();
		ioSession.writeAndFlush(packet);
	}

	public void writeToClient(ResponseCode code, MessageLite message) {
		for (Player player : playerList) {
			player.write(code, message);
		}
	}

	protected Position randomPosition() {
		if (positionList == null) {
			positionList = new ArrayList<>();
			int num = 0;
			GameType gameType = GameType.getByValue(gameId);
			switch (gameType) {
				case XUENIU:
					num = 4;
					break;
				case XUEZHAN:
					num = 4;
					break;
				case ZJH: //ZJh
					num = 6;
					break;
				case CLASS_NIU:
					num = 5;
					break;
				case GRAD_NIU:
					num = 5;
					break;
				default:
					num = 4;
					break;
			}
			for (int i = 1; i <= num; i++) {
				positionList.add(Position.getByValue(i));
			}
		}
		int size = positionList.size();
		if (size == 0) {
			return null;
		}
		int random = Randomizer.nextInt(size);
		return positionList.remove(random);
	}

	public boolean isPrivateRoom() {
		return this.creator != null;
	}
	
	/** 是否已经在桌子上 */
	public boolean isAtDesk(int playerId) {
		for (Player player : playerList) {
			if(player.getPlayerId() == playerId){
				return true;
			}
		}
		return false;
	}
	
	public synchronized void onPlayerOtherLogin(Player p){
		if(isCreated()){
			return;
		}
		LobbyGameManager.getInst().playerLeaveGameRoom(p);
		LobbyGameManager.getInst().playerLeavePriRoom(GameType.getByValue(gameId), p);
		//这个地方只有私房和桌子组房才会进   匹配房离开只可能是桌子创建
		if (this.creator == p) {
			removePlayer(p);
			disbandDesk();
		} else {
			int pos = getPosition(p);
			removePlayer(p);
			notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(pos));
			GroupDeskManager.getIns().onPlayerLeaveDesk(p,this,false);
		}
	}

	public void playerLeave(Player p) {
		if(!isCreated()){
			LobbyGameManager.getInst().playerLeaveGameRoom(p);
			LobbyGameManager.getInst().playerLeavePriRoom(GameType.getByValue(gameId), p);
			//这个地方只有私房和桌子组房才会进   匹配房离开只可能是桌子创建
			if (this.creator == p) {
				disbandDesk();
			} else {
				int pos = getPosition(p);
				notifyMessage(ResponseCode.COUPLE_OPPOSITE_LEAVE, CommonCreator.createPBInt32(pos));
				removePlayer(p);
				GroupDeskManager.getIns().onPlayerLeaveDesk(p,this,false);
			}
		}else{
			writeToLogic(new CocoPacket(RequestCode.LOGIC_PLAYER_LEAVE_DESK, null, p.getPlayerId()));
		}
	}

//	public boolean playerMoneyEnough(Player player) {
//		if (player.getCoin() < getEnterLimit() * getReq().getRequest().getEnterTimes()) {
//			return false;
//		}
//		return true;
//	}
	
	public long getCheckMoneyNum(){
		if (creator == null) {
			return 0;
		}
		long coin = 0;
		GameType gameType = GameType.getByValue(gameId);
		switch (gameType) {
			case XUENIU:
			case XUEZHAN:
				coin = req.getRequest().getBaseScore() * getReq().getRequest().getEnterTimes();
				break;
			case ZJH: //ZJh
				coin = req.getZjhReq().getLimitPots() * getReq().getZjhReq().getEnterPots();
				break;
			case CLASS_NIU:
				coin = req.getNiuReq().getBaseScore() * getReq().getNiuReq().getEnterTimes();
				break;
			default:
				break;
		}
		return coin;
	}

	public void disbandDesk() {
		playerList.forEach(e -> {
//			e.setRoomId(0);
//			e.setDeskInfo(null);
			e.updateLoginStatus(LoginStatusConst.ENTER_HALL);
		});
		disbandTime = 0;
		DeskManager.getInst().removeDesk(this);
		notifyMessage(ResponseCode.LOBBY_DISBAND_SUCC, null);
	}

	public Position removePlayer(Player player) {
		playerList.remove(player);
		player.setDeskInfo(null);
//		player.setRoomId(0);
		if(player2Position != null){
			Position position = player2Position.remove(player.getPlayerId());
			positionList.remove(position);
			returnPosition(position);
			return position;
		}
		return null;
	}

	private void returnPosition(Position position) {
		positionList.add(position);
	}


	public void notifyPlayerEnter() {
		notifyMessage(ResponseCode.COUPLE_ENTER_DESK, createPBPlayerInfoList(true));
	}

	public CoupleMajiang.PBLoigcCreareRoomReq createRoomReq() {
		CoupleMajiang.PBLoigcCreareRoomReq.Builder builder = CoupleMajiang.PBLoigcCreareRoomReq.newBuilder();
		builder.setPlayerList(createPBPlayerInfoList(false));
		builder.setReq(createDeskReq());
		builder.setRoomId(getDeskId());
		return builder.build();
	}

	private Lobby.PBCreateDeskReq createDeskReq() {
		return req;
	}

	public Lobby.PBCreateDeskReq getReq() {
		return req;
	}

	private Common.PBPlayerInfoList createPBPlayerInfoList(boolean isFront) {
		Common.PBPlayerInfoList.Builder builder = Common.PBPlayerInfoList.newBuilder();
		playerList.forEach(e -> builder.addPlayerInfoList(createPBPlayerInfo(e,isFront)));
		if (creator != null) {
			builder.setCreatorId(creator.getPlayerId());
		}
		return builder.build();
	}

	private Common.PBPlayerInfo createPBPlayerInfo(Player player,boolean isFront) {
		Common.PBPlayerInfo.Builder builder = Common.PBPlayerInfo.newBuilder();
		builder.setPlayerId(player.getPlayerId());
		builder.setName(player.getName());
		builder.setIcon(player.getIcon());
		builder.setCoin((int) player.getCoin());
		builder.setGender(player.getGender().getValue());
//		if(isFront){
//			if(WordBadUtil.hasBadProvince(player.getProvince())){
//				builder.setProvince(WordBadUtil.DEFAULT_PROVINCE);
//				builder.setCity(WordBadUtil.DEFAULT_CITY);
//			}else{
//				builder.setProvince(player.getProvince());
//				builder.setCity(player.getCity());
//			}
//		}else{
			builder.setProvince(player.getProvince());
			builder.setCity(player.getCity());
//		}
		builder.setPosition(player2Position.get(player.getPlayerId()).getValue());
		builder.setIsLeave(player.isLogout());
		builder.setChannelId(player.getChannelId());
		builder.setPackageId(player.getPackageId());
		builder.setDevice(player.getDevice());
		builder.setIp(player.getIp());

		return builder.build();
	}

	public Lobby.PBDeskInfo createDeskInfo() {
		Lobby.PBDeskInfo.Builder builder = Lobby.PBDeskInfo.newBuilder();
		if (creator != null) {
			builder.setCreator(creator.getName());
			builder.setCreateId(creator.getPlayerId());
		} else {
			builder.setCreator("");
			builder.setCreateId(0);
		}
		builder.setReq(req);
		builder.setGameId(gameId);
		return builder.build();
	}

	public void notifyMessage(ResponseCode code, MessageLite message) {
		for (Player player : playerList) {
			player.write(code, message);
		}
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public void init(int gameId, int roomId, ChannelHandlerContext ioSession) {
		this.gameId = gameId;
		this.roomId = roomId;
		setIoSession(ioSession);
	}
	
	public boolean isCreated() {
		return getBindServerSession() != null;
	}
}
