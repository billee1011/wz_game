package server;

/**
 * Created by WZ on 2016/8/25.
 */
public enum EServerType {
	CENTER((byte)1),
	LOGIN((byte)2),
	ENTITY((byte)3),
	AI((byte)4),
	LOG((byte)5),
	WORLD((byte)6),
	BACKUP((byte)7),
	AGENT((byte)8),
	;

	private byte value;

	public byte getValue() {
		return this.value;
	}

	private EServerType(byte value) {
		this.value = value;
	}

	public static EServerType getByValue(int value) {
		for (EServerType type : values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		return null;
	}

}
