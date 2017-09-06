package define;

import logic.room.*;

import java.util.concurrent.atomic.AtomicInteger;

import define.constant.GameModuleConst;

/**
 * Created by Administrator on 2016/12/12.
 */
public enum GameType {
	COUPLE_MJ(1, () -> new CoupleRoomFacade(),GameModuleConst.MODULE_MAJIANG),
	XUENIU(2, () -> new XueNiuRoomFacade(),GameModuleConst.MODULE_MAJIANG),
	XUEZHAN(3, () -> new XueZhanRoomFacade(),GameModuleConst.MODULE_MAJIANG),
	NIUNIU(4, () -> new NiuniuRoomFacade(),GameModuleConst.MODULE_100),
	DDZ(5, () -> new DdzRoomFacade(),GameModuleConst.MODULE_POKER),
	ZJH(6, () -> new ZjhRoomFacade(),GameModuleConst.MODULE_POKER),
	COUPLE_DDZ(7, () -> new CoupleDdzRoomFacade(),GameModuleConst.MODULE_POKER),
	LZ_DDZ(8, () -> new LzDdzRoomFacade(),GameModuleConst.MODULE_POKER),
	CLASS_NIU(9,() -> new ClassNiuRoomFacade(),GameModuleConst.MODULE_POKER),       //经典牛牛
	GRAD_NIU(10,() -> new GrabNiuRoomFacade(),GameModuleConst.MODULE_POKER),		//抢庄牛牛
	;

	private int value;

	private AtomicInteger gameCount = new AtomicInteger(1);

	public int getGameCount() {
		return gameCount.getAndIncrement();
	}

	private RoomFacadeFactory factory;
	
	private int module;

	public int getValue() {
		return this.value;
	}

	public int getModule() {
		return module;
	}

	public GameRoomInterface createFacade() {
		if (factory == null) {
			return null;
		}
		return factory.create();
	}

	GameType(int value, RoomFacadeFactory factory,int module) {
		this.value = value;
		this.factory = factory;
		this.module = module;
	}

	interface RoomFacadeFactory {
		GameRoomInterface create();
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
