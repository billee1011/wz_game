package logic.poker.zjh;

/**
 * Created by hhhh on 2017/3/24.
 */
public enum ZjhGroupType {
	SPECIAL(0),//特殊
	LEOPARD(1),//豹子
	STRAIGHTGOLD(2),//顺金
	GOLDFLOWER(3),//金花
	STRAIGHT(4),//顺子
	COUPLE(5),//对子
	SIGNLE(6),//散牌
	;

	private int value;

	ZjhGroupType(int value){
		this.value = value;
	}

	public int getValue(){
		return this.value;
	}
}
