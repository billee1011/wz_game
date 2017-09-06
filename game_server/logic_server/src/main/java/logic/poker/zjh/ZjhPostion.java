package logic.poker.zjh;

import java.util.Set;

/**
 * Created by hhhh on 2017/3/24.
 */
public enum ZjhPostion {
	ONE(1),
	TWO(2),
	THREE(3),
	FOUT(4),
	FIVE(5),
	SIX(6),;

	private int value;

	ZjhPostion(int value) {
		this.value = value;
	}

	public ZjhPostion getNextPos(){
		if(this.getValue() == 6){
			return ONE;
		}
		return getByValue(value + 1);
	}

	public ZjhPostion getNextPos(int maxValue){
		if(this.getValue() == maxValue){
			return ONE;
		}
		return getByValue(value+1);
	}

	public static ZjhPostion getByValue(int value){
		for(ZjhPostion postion:values()){
			if(postion.getValue() == value){
				return  postion;
			}
		}
		return  null;
	}

	public static ZjhPostion getFree(Set<ZjhPostion> set) {
		for (ZjhPostion postion : values()) {
			if (!set.contains(postion)) {
				return postion;
			}
		}
		return null;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
