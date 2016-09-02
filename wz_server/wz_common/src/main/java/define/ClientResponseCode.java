package define;

/**
 * Created by WZ on 2016/9/2 0002.
 */
public enum ClientResponseCode {
	LOGIN_NO_ROLE(0x1001),
	;


	private int value;

	private ClientResponseCode(int vlaue){
		this.value = value;
	}

	public int getValue(){
		return this.value;
	}

	public static ClientResponseCode getByValue(int value){
		for (ClientResponseCode code :values() ) {
			if(code.getValue() == value){
				return code;
			}
		}
		return null;
	}

}
