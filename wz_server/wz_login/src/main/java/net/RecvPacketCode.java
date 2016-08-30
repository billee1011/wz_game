package net;

/**
 * Created by Administrator on 2016/8/29 0029.
 */
public enum RecvPacketCode {
	LOGIN_PASSWORLD(1),
	REQUEST_CHAR_LIST(2),
	CREATE_CHAR(3),;

	private int value;

	public int getValue() {
		return value;
	}

	private RecvPacketCode(int value) {
		this.value = value;
	}

	public RecvPacketCode getByValue(int value) {
		for (RecvPacketCode code : values()) {
			if (code.getValue() == value) {
				return code;
			}
		}
		return null;
	}
}
