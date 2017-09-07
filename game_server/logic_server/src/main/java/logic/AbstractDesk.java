package logic;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import config.CoupleRoomInfoProvider;
import config.bean.CoupleRoom;
import logic.majiong.PlayerInfo;
import proto.creator.CommonCreator;
import protocol.s2c.ResponseCode;
import util.IDGenerator;
import util.MiscUtil;

/**
 * Created by Administrator on 2017/2/17.
 */
public abstract class AbstractDesk implements Desk {
//	public static final int DESK_ID_BEGIN = 100000000;
	private boolean personal;	// 是否私房

	protected int createTime;

	protected int confId;

	private static IDGenerator idGenerator = new IDGenerator();
	protected int deskId;

	protected ScheduledFuture<?> overMyself = null;		// 結束自己的定時器
	
	protected AtomicBoolean isDestroyed = new AtomicBoolean(false);

	public AbstractDesk() {
		this(0,0);
	}

	public boolean isPersonal() {
		return personal;
	}

	public void setPersonal(boolean personal) {
		this.personal = personal;
	}

	public AbstractDesk(int deskId,int confId) {
		this.createTime = MiscUtil.getCurrentSeconds();
		this.confId = confId;
		this.deskId = deskId;// getGameType().getValue() * DESK_ID_BEGIN + idGenerator.getNextId();
	}

	@Override
	public int getDeskId() {
		return this.deskId;
	}

	public void setDeskId(int deskId){
		this.deskId = deskId;
	}

	protected int getBaseScore() {
		return CoupleRoomInfoProvider.getInst().getBaseScoreOfRoom(getConfId());
	}

	//实际获得赢钱的百分比
	protected int getGainRate() {
		CoupleRoom conf = CoupleRoomInfoProvider.getInst().getRoomConf(getConfId());
		if (conf == null) {
			return 95;				//默认获得赢钱的95%
		}
		return 100 - conf.getTax_rate();
	}

	@Override
	public void playerMoneyChange(int playerId, int coin, boolean add) {
		PlayerInfo info = getPlayerInfo(playerId);
		if (info == null) {
			return;
		}
		info.updateCoin(coin, add, 0);
		playerMoneyChangeHook(info);
	}

	/** 这个当游戏有飘字结算的时候，尽量沟通不要发，客户端自己+-算 */
	public void syncAllPlayerMoney() {
		for (PlayerInfo info : getPlayerList()) {
			if (info == null) {
				continue;
			}
			info.write(ResponseCode.COUPLE_UPDATE_DESK_MONEY, CommonCreator.createPBPairList(getAllPlayerMoney()));
		}
	}


	@Override
	public boolean isAllPlayerLeave() {
		return false;
	}

	protected abstract boolean destroyDesk();

	protected abstract void onGameEnd();

	protected abstract Map<Integer, Integer> getAllPlayerMoney();

	public int getConfId() {
		return confId;
	}

	protected abstract void playerMoneyChangeHook(PlayerInfo info);

	@Override
	public int getCreateTime() {
		return this.createTime;
	}

	protected abstract void disbandDesk();
	
	protected void stopOverMyselfFuture() {
		if (overMyself != null) {
			overMyself.cancel(true);
			overMyself = null;
		}
	}
	
	public abstract void disBankDeskTimeOver();
}
