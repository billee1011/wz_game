package net.message;

/**
 * Created by WZ on 2016/8/25.
 */
public enum CenterModule {
	REGISTER_SERVER((byte)1),;

	private byte value;

	public byte getValue() {
		return this.value;
	}

	private CenterModule(byte value) {
		this.value = value;
	}

	public static CenterModule getByValue(int value) {
		for (CenterModule module : values()) {
			if( module.getValue() == value){
				return module;
			}
		}
		return null;
	}

}
