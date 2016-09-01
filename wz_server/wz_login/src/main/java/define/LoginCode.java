package define;

/**
 * Created by WZ on 2016/9/1 0001.
 */
public enum LoginCode {
	LOGIN_PASSWORD(1),
	;

	private int value;

	public int getValue(){
		return value;
	}

	private LoginCode(int value){
		this.value = value;
	}

	public static LoginCode getByValue(int value){
		for (LoginCode code :values() ) {
			if( code.getValue() == value){
				return code;
			}
		}
		return null;
	}

}
