package logic.poker.zjh;

/**
 * Created by hhhh on 2017/4/3.
 */
public enum  GameState {
	WAIT(1),	//等待游戏开始
	BEGIN(2),   //游戏可以开始
	START(3),	//游戏开始
	OVER(4),;	//结束

	private int value;

	GameState(int value){
		this.value = value;
	}

	public int getValue(){
		return this.value;
	}
}
