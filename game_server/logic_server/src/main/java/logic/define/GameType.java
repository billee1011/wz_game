package logic.define;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2016/12/12.
 */
public enum GameType {
	COUPLE_MJ(1),
	XUELIU(2),
	XUEZHAN(3),
	NIUNIU(4),
	DDZ(5),
	ZJH(6),
	COUPLE_DDZ(7),
	LZ_DDZ(8),
	CLASS_NIU(9),
	GRAB_NIU(10),
	;

	GameType(int value) {
		this.value = value;
	}

	private int value;

	private AtomicInteger gameCount = new AtomicInteger(1);

	public int getGameCount() {
		return gameCount.getAndIncrement();
	}

	public int getValue() {
		return this.value;
	}

	public static GameType getByValue(int value) {
		for (GameType gameType : values()) {
			if (gameType.getValue() == value) {
				return gameType;
			}
		}
		return null;
	}
}
