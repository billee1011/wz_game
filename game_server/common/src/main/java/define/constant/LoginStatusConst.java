package define.constant;

public interface LoginStatusConst {
	/** 已经退出游戏*/
	public static final int EXIT_GAME = 0;
	
	/** 大厅 */
	public static final int ENTER_HALL = 1;
	
	/** 登录之后进入其他游戏从 100 开始  + roomId */
	public static final int AT_GAME_ROOM_ING = 100;
	
	/** 进入匹配  + roomId */
	public static final int AT_ENTER_MATCH = 10000;
	
	/** 私房游戏中  + gameId 对庄牛牛私房9 */
	public static final int AT_PRI_GAME_ING = 100000;
}
