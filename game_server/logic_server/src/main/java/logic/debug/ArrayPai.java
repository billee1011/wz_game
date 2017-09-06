package logic.debug;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import logic.debug.bean.DdzArray;
import logic.debug.bean.MjArray;
import logic.debug.bean.NiuNiuArray;
import logic.define.GameType;
import logic.majiong.define.MJType;
import logic.poker.PokerCard;

public class ArrayPai {
	private static ArrayPai instance = new ArrayPai();
	public static ArrayPai getInst() {
		return instance;
	}
	
	// 二人麻将
	private MjArray coupleMj; 
	// 四人麻将
	private MjArray xueliuMj;
	
	//抢庄类牛
	private NiuNiuArray niuniu;
	
	// 炸金花
	private NiuNiuArray zjh;
	
	// 斗地主 
	private DdzArray ddz;
	
	// lz斗地主
	private DdzArray ddzLz;
	
	// 2人斗地主
	private DdzArray ddz2;
	
	public void clean(){
		coupleMj = null;
		xueliuMj = null;
		niuniu = null;
		zjh = null;
		ddz = null;
		ddzLz = null;
		ddz2 = null;
	}
	
	public void arrayPai(GameType gameType, String value) {
		switch (gameType) {
		case COUPLE_MJ:
			coupleMj = new MjArray(value);
			break;
		case XUELIU:
		case XUEZHAN:
			xueliuMj = new MjArray(value);
			break;
		case NIUNIU:
		case CLASS_NIU:
		case GRAB_NIU:
			niuniu = new NiuNiuArray(value);
			break;
		case ZJH:
			zjh = new NiuNiuArray(value);
			break;
		case DDZ:
			ddz = new DdzArray(value);
			break;
		case LZ_DDZ:
			ddzLz = new DdzArray(value);
			break;
		case COUPLE_DDZ:
			ddz2 = new DdzArray(value);
			break;
		default:
			break;
		}
	}

	public void setZhuangForXueliuMj(int pos) {
		if (xueliuMj == null) {
			return;
		}
		pos = xueliuMj.getZhuangPos();
	}
	
	public void arrayPaiForMj(Queue<Integer> result, List<Integer> paiPool, MJType type) {
		MjArray mj;
		switch (type) {
		case COUPLE_MJ:
			mj = coupleMj;
			break;
		case XUELIU:
			mj = xueliuMj;
			break;
		default:
			return;
		}
		if (mj != null){
			result.addAll(mj.getCardList());
			for (int i = 0; i < mj.getCardList().size(); i++) {
				Iterator<Integer> it = paiPool.iterator();
				while (it.hasNext()) {
					if (it.next() != mj.getCardList().get(i)) {
						continue;
					}
					it.remove();
					break;
				}
			}
		}
	}
	
	public void arrayPaiForPoker(Queue<PokerCard> result, List<PokerCard> paiPool, GameType type) {
		List<PokerCard> cardList = null;
		switch (type) {
		case NIUNIU:
			if (niuniu != null) {
				cardList = niuniu.getCardList();
			}
			break;
		case ZJH:
			if (zjh != null) {
				cardList = zjh.getCardList();
			}
			break;
		case DDZ:
			if (ddz != null) {
				cardList = ddz.getCardList();
			}
			break;
		case LZ_DDZ:
			if (ddzLz != null) {
				cardList = ddzLz.getCardList();
			}
			break;
		case COUPLE_DDZ:
			if (ddz2 != null) {
				cardList = ddz2.getCardList();
			}
			break;
		default:
			break;
		}

		if (cardList != null) {
			result.addAll(cardList);
			for (int i = 0; i < cardList.size(); i++) {
				Iterator<PokerCard> it = paiPool.iterator();
				while (it.hasNext()) {
					if (it.next() != cardList.get(i)) {
						continue;
					}
					it.remove();
					break;
				}
			}
		}
	}
	
	public PokerCard getDDZLzPaiForPoker(GameType type) {
		switch (type) {
		case LZ_DDZ:
			if (ddzLz != null) {
				return ddzLz.getLzCard();
			}
			break;
		default:
			break;
		}
		return null;
	}
	
	public boolean isOpenDebugPai(GameType type){
		boolean open = false;
		switch (type) {
		case NIUNIU:
			if (niuniu != null) {
				open = true;
			}
			break;
		case ZJH:
			if (zjh != null) {
				open = true;
			}
			break;
		case DDZ:
			if (ddz != null) {
				open = true;
			}
			break;
		case LZ_DDZ:
			if (ddzLz != null) {
				open = true;
			}
			break;
		case COUPLE_DDZ:
			if (ddz2 != null) {
				open = true;
			}
			break;
		default:
			break;
		}
		return open;
	}
	
}
