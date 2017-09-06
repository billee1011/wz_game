package logic.poker;

import logic.AbstractDesk;
import logic.majiong.PlayerInfo;

/**
 * Created by Administrator on 2016/12/13.
 */
public abstract class PokerDesk extends AbstractDesk {

	public PokerDesk(int deskId,int confId) {
		super(deskId,confId);
	}

	public PokerDesk() {

	}

	@Override
	public boolean isGameing() {
		return false;
	}
	
	public abstract void enterPlayer(PlayerInfo player);

	public abstract void playerLogout(PlayerInfo player);

	@Override
	public abstract void playerLeave(PlayerInfo player);

	@Override
	protected boolean destroyDesk() {
		return false;
	}

	@Override
	protected void onGameEnd() {
		// do nothing , implement is this class for  niuniu
	}

	@Override
	public boolean isTiyan() {
		return false;
	}

}
