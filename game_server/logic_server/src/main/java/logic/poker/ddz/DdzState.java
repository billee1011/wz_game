package logic.poker.ddz;

/**
 * Created by think on 2017/4/14.
 */
public enum DdzState {
	READY(1),                    //准备阶段
	DEAL_CARD(2, 2),            //发牌阶段
	CALL_LORD(3, 10),            //叫地主
	ROB_LORD(4, 10),            //抢地主
	ROB_END(5, 2),                //抢地主结束动画
	//	ADD_BONUS(6, 10),            //加倍阶段
	DISCARD(7, 15),            //出牌阶段
	CALCULATE(8, 10),            //结算阶段    不知道需要不需要啊
	LIUJU(9, 3),            //流局给3秒播放动画之后重新发牌
	END(10, -1),            //流局给3秒播放动画之后重新发牌
	;            //出牌阶段

	private int id;

	private int seconds;

	DdzState(int id) {
		this(id, -1);
	}

	DdzState(int id, int seconds) {
		this.id = id;
		this.seconds = seconds;
	}

	public int getSeconds() {
		return seconds;
	}

	public int getId() {
		return id;
	}
}
