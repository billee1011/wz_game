package define.constant;

public interface MessageConst {
	/** 银行钱不够 */
	public static final int BANK_MONEY_NOT_ENOUGH = 210;
	
	/** db中不存在取出玩家 */
	public static final int DB_NOT_OUT_PLAYER = 211;
	
	/** db中不存在存入出玩家 */
	public static final int DB_NOT_IN_PLAYER = 212;
	
	/** 转账id不存在 */
	public static final int NO_TRANSFER_ID = 213;
	
	/** 验证码错误 */
	public static final int CODE_ERROR = 10010;
	
	/** 对不起，该手机号码已经被注册 */
	public static final int PHONE_HAS_USED = 10012;
	
	/** 您的原银行密码填写错误 */
	public static final int ORIGINAL_PASSWORD_ERROR = 10019;
	
	
	/** 请输入正确的银行密码 */
	public static final int BANK_PASSWORD_ERROR = 10025;
	
	/** 兑换申请成功，请注意查收支付宝账号 */
	public static final int EXCHANGE_SUC = 10031;
	
	
	/** 对不起，您当前金额不满足上庄条件 */
	public static final int ROB_ZHUANG_COIN_NOT_ENOUGH = 10044;
	
	/** 申请上庄成功，请等待下一局游戏开始 */
	public static final int ROB_ZHUANG_SUC = 10045;
	
	/** 申请成功，请等待其他玩家下庄 */
	public static final int ROB_ZHUANG_WAIT_OTHER = 10046;
	
	/** 您当前连庄次数已满10局，不能继续连庄 */
	public static final int BANNER_ROUNDS_10 = 10049;
	
	/** 申请下庄成功，下局游戏开始将自动下庄 */
	public static final int PLAYER_GIVE_UP_BANNER = 10048;
	
	/** 对不起，您当前金额已低于申请上庄条件，被迫取消申请 */
	public static final int ZHUANG_COIN_NOT_ENOUGH = 10050;
	
	/** 取消申请成功 */
	public static final int PLAYER_CANCEL_BANNER = 10052;
	
	/** 该下注区域已满 */
	public static final int CHIP_FULL = 10055;
	
	/** 您当前金币不足，被强制下庄 */
	public static final int BANNER_COIN_NOT_ENOUGH = 10056;
	
	/** 您已经达到下注金额上限 */
	public static final int CHIP_MAX = 10057;
	
	/** 对不起，您兑换后的金额不足以支付手续费，无法完成兑换 */
	public static final int EXCHANGE_COIN_NOT_ENOUGH = 10058;
	
	/** 对不起，您当前还有还有未完成的游戏，不能进行存款操作 */
	public static final int AT_GAME_ROOM_ING_SAVE_MONEY = 10059;
	
	/** 对不起，您当前还有还有未完成的游戏，不能进行兑换操作 */
	public static final int AT_GAME_ROOM_ING = 10060;
	
	/** 充值成功 */
	public static final int ACTION_ADD_COIN = 10061;
	
	/** 您输入的房号不存在，请查证后重新输入 */
	public static final int DESK_ID_ERROR = 10062;
	
	/** 对不起，该房间已经解散，无法进入 */
	public static final int DESK_DESTROY = 10063;
	
	
	/** 对不起，该房间人数已满，无法进入 */
	public static final int DESK_FULL = 10064;
	
	
	/** 对不起，该支付宝账号已经与其他账号绑定，无法再次绑定 */
	public static final int ALIPAY_HAS_BIND = 10069;
	
	
	/** 游客账号，无法进行转账 */
	public static final int VISITOR_NO_TRANSFER = 10071;

	
	/** 对不起，转账功能仅限于玩家与代理间使用，请确定对方账号身份 */
	public static final int TRANSFER_IDENTITY_ERROR = 10072;
	
	/** 银行密码错误，请重新输入 */
	public static final int TRANSFER_PASSWORD_ERROR = 10073;
	
	/** 银行存款不足 */
	public static final int TRANSFER_COIN_NOT_ENOUGH = 10074;
	
	
	/** 对不起，因为您存在不当操作被添加进黑名单无法兑换，如有疑问请联系客服 */
	public static final int EXCHANGE_BLACKLIST = 10078;
	
	/** 兑换失败，请查看邮件或联系客服*/
	public static final int EXCHANGE_RESTORE_FAIL = 10079;
	
	/** 回放记录不存在。 */
	public static final int RECORD_NO_EXIST = 10080;
	
	/** 对不起，您当前金额不足以续押，请重新进行下注 */
	public static final int CHIP_NOT_ENOUGH = 10081;
	
	/** 该游戏正在维护中 */
	public static final int SERVER_STOP = 10082;
	
	/** 对不起，该账号已经被绑定 */
	public static final int PLAYER_PHONE_HAS_REGISTER = 10083;
	
	/** 只能是5000的倍数 */
	public static final int EXCHANGE_NO_5000_MULTIPLE = 10086;
	
	/** 游戏已经开始不能加入 */
	public static final int GAME_STARTED = 10090;
	
	/** 该转账用户无效 */
	public static final int TRANSFER_DIFF_PLATFORM = 10091;
	
	/** 转账操作频繁 */
	public static final int TRANSFER_TIME_OUT = 10092;
	
	/** 投诉成功 */
	public static final int COMPLAINT_SUC = 10093;

	/** 该代理不存在 */
	public static final int COMPLAINT_AGENT_ERROR = 10094;

	/** 您已经投诉过该代理，今日无法再次投诉 */
	public static final int COMPLAINT_AGENT_LIMIT = 10095;

	/** 您已经投诉过该客服，今日无法再次投诉 */
	public static final int COMPLAINT_KEFU_LIMIT = 10096;
	
	/** 对不起，您当前还有还有未完成的游戏，不能进行转账 */
	public static final int AT_GAME_ROOM_ING_AGENT_PAY = 10097;
	
	/** 升級账号 验证码超时 */
	public static final int REGISTER_TIME_OUT = 10098;
	
	/** 斗地主超时自动出牌 */
	public static final int DDZ_TIME_OUT_AUTO_DISCARD = 10099;
	
	/** 转账id不存在 */
	public static final int ERROR_TRANSFER_ID = 10100;
	
	/** 该功能暂不可用 */
	public static final int PROVINCE_CLOSE = 10101;
	
}
