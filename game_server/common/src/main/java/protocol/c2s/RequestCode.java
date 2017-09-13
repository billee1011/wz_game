package protocol.c2s;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import define.AppId;

public enum RequestCode {
	PING(100, AppId.GATE),
	RESPONSE_TO_CLIIENT_CODE(-1, AppId.GATE),

	ACCOUNT_BEGIN(100),
	ACCOUNT_LOGIN(101, AppId.CENTER),
	ACCOUNT_LOGIN_RESULT(102, AppId.GATE),
	ACCOUNT_TEST(103),
	ACCOUNT_RETRIEVE_PASSWORD(259),
	ACCOUNT_MODIFY_PASSWORD(260),
	ACCOUNT_MODIFY_ICON(261, AppId.CENTER),
	ACCOUNT_BIND_ALI_PAY(262, AppId.CENTER),
	ACCOUNT_MODIFY_BANK_PASSWORD(263, AppId.CENTER),
	ACCOUNT_SAVE_MONEY(264, AppId.CENTER),
	ACCOUNT_WITHDRAW_MONEY(265, AppId.CENTER),
	ACCOUNT_MODIFY_GENDER(266, AppId.CENTER),
	ACCOUNT_EXCHANGE(268, AppId.CENTER),                            //兑换
	ACCOUNT_GAIN_VALID_CODE(269),                        //获取验证码
	ACCOUNT_GET_WX_PRE_ID(270),                        //获取pre id
	ACCOUNT_UPDATE_DATA(271, AppId.CENTER),                        //获取pre id
	ACCOUNT_GENE_PAY_ORDER(272, AppId.CENTER),                    //生成订单号
	ACCOUNT_REGISTER_GAME_ID(273, AppId.CENTER),                  //接收玩家渠道号
	ACCOUNT_ADD_COIN(274, AppId.CENTER),                  //接收玩家渠道号
	ACCOUNT_MODIFY_NICK_NAME(275, AppId.CENTER),                        //修改昵称
	ACCOUNT_GET_DYNAMIC_CONFIG(276, AppId.CENTER),                        //获取动态配置
	ACCOUNT_GET_RANDOM_NAME(277, AppId.CENTER),                             //获取随机名字
	ACCOUNT_GET_ANNOUNCEMENT(278, AppId.CENTER),                            //获取公告
	ACCOUNT_REQUEST_AGENT_INFO(279, AppId.CENTER),                            //请求代理
	ACCOUNT_COMPLAINT(280, AppId.CENTER),                     // 投诉代理或客服
	ACCOUNT_OPEN_BANK(281, AppId.CENTER),                     // 开启保险箱

	/////////////////////////////////////////////
	////////////////////////////////////////////
	LOBBY_BEGIN(512),
	LOBBY_ENTER_GAME(513, AppId.CENTER),
	LOBBY_ENTER_GAME_ROOM(514, AppId.CENTER),
	LOBBY_RANK_INFO(515, AppId.CENTER),
	LOBBY_FAST_GAME(516),
	LOBBY_LEAVE_DESK(517, AppId.CENTER),
	LOBBY_SWITCH_OPPONENT(518, AppId.CENTER),
	LOBBY_PLAYER_LOGOUT(519),
	LOBBY_PLAYER_RELOGIN(520),
	LOBBY_CREATE_ROOM_DESK(521, AppId.CENTER),               //申请创建房间
	LOBBY_ENTER_ROOM_DESK(522, AppId.CENTER),
	LOBBY_GET_ROOM_INFO(523, AppId.CENTER),
	LOBBY_TRANS_VOICE(524, AppId.CENTER),
	LOBBY_DISBAND_DESK(525, AppId.CENTER),
	LOBBY_AGREE_DISBAND(526, AppId.CENTER),
	LOBBY_REFUSE_DISBAND(527, AppId.CENTER),
	LOBBY_GET_PEOPLE_NUM(529, AppId.CENTER),
	LOBBY_ENTER_MODULE(530, AppId.CENTER),
	LOBBY_EXIT_MODULE(531, AppId.CENTER),
	LOBBY_GET_ROOM_SCORE_LIST(532, AppId.CENTER),                //战绩列表
	LOBBY_GET_SCORE_DETAIL(533, AppId.CENTER),
	GAME_RECORD(534, AppId.CENTER),    //回放
	LOBBY_ZJH_BEGIN_GAME(535, AppId.CENTER),                                         //私房游戏开始


	MAIL_BEGIN(1024),                                //邮件模块
	MAIL_ALL(1025, AppId.CENTER),
	MAIL_DELETE(1026, AppId.CENTER),
	MAIL_CONTACT_KEFU(1027, AppId.CENTER),                //联系客服
	MAIL_GAIN_MAIL(1028, AppId.CENTER),                //获取邮件
	MAIL_READ_MAIL(1029, AppId.CENTER),                //获取邮件
	MAIL_GET_ALL_KEFU_MESSAGE(1030, AppId.CENTER),    //获取所有客服的
	MAIL_SEND_AGENT_MESSAGE(1031, AppId.CENTER),    //发送给代理商消息
	AGENT_TRANSFER_REQUEST(1032, AppId.CENTER),    //转账申请
	AGENT_TRANSFER_MESSAGE(1033, AppId.CENTER),    //转账记录
	//////////////////////////////////////////////
	NIUNIU_BEGIN(1280),
	NIUNIU_LOWER_CHIP(1281),
	NIUNIU_ROB_BANNER(1282),
	NIUNIU_GIVE_UP_BANNER(1283),
	NIUNIU_CANCEL_BANNER(1284),
	NIUNIU_GET_HIS_RECORD(1285),
	NIUNIU_RESET_NIUNIU(1286),
	NIUNIU_GET_RANK_LIST(1287),
	NIUNIU_RENEWED(1288),                // 牛牛续押


	XUENIU_BEGIN(1536),
	XUENIU_CHOSE_CARDS_SWITCH(1537),
	XUENIU_CHOSE_TYPE(1538),
	XUENIU_GET_DISBAND_INFO(1539, AppId.CENTER),

//	CENTER_VALID_LOGIN(1795, AppId.CENTER),                        //向center请求登录验证 如果不成功 断开连接

	DDZ_ROB_LORD(2049),                //抢地主
	DDZ_DISCARD_CARD(2050),            //斗地主出牌
	DDZ_PASS(2051),                    //斗地主pass
	DDZ_JIABEI(2052),                    //斗地主加倍
	DDZ_TUOGUAN(2053),                //斗地主托管
	DDZ_CANCEL_TUOGUAN(2054),       //斗地主取消托管

	LOGIC_ROOM_GAME_START(2309),                               //房间开始游戏

	LOGIC_GIVE_UP(2311),                                        //玩家认输
	LOGIC_CONTINUE_GAME(2312),                                  //玩家请求继续游戏


	//扎金花
	ZJH_ADD_GOLD(2817),               //加注
	ZJH_LOOK_CARD(2818),          //看牌
	ZJH_COMPARE_CARD(2819),       //比牌
	ZJH_GIVE_UP(2820),            //弃牌
	ZJH_SHOW_CARDS(2821),         //亮牌
	ZJH_ALL_GOLD_IN(2823),                             //孤注一掷
	ZJH__FULL_PRESSURE(2824),                             //压满

	GRAB_NIU_GRAB_ZHUANG(3074),                // 抢庄
	GRAB_NIU_ADD_BET(3075),                    // 加注
	GRAB_NIU_PLAYER_CAL(3076),                // 玩家点击是否有牛

	LOBBY_DEBUG_ARRAY_PAI(4353, AppId.CENTER),                   // 客戶端请求排牌

	LOBBY_DEBUG_CHECK_FAN_TYPE(4356, AppId.CENTER),                   // 客戶端请求檢測牌型


	//-------服务器内部通信协议号范围10100-10800---------gate做了限制----------
	CENTER_REGISTER_SERVER(10101, AppId.CENTER),
	CENTER_DISPATCH_GATE(10102, AppId.CENTER),                   //发送信息给center 要求分配gate给登录
	CENTER_CREATE_DESK_SUCC(10103, AppId.CENTER),                //桌子创建成功\
	CENTER_PLAYER_LEAVE_ROOM(10104, AppId.CENTER),               //玩家离开房间成功
	CENTER_PLAYER_LOGOUT(10105, AppId.CENTER),                   //玩家登出游戏
	CENTER_UPDATE_PLAYER_COIN(10106, AppId.CENTER),              //更新玩家的货币
	CENTER_CREATE_ROOM_DESK_SUCC(10107, AppId.CENTER),           //center桌子解散
	CENTER_ROOM_DESK_DISBAND(10108, AppId.CENTER),               //center桌子解散
	CENTER_ROOM_DESK_GAME_END(10109, AppId.CENTER),              //center桌子到局数了需要解散
	CENTER_SERVER_PING(10110, AppId.CENTER),                     //心跳
	CENTER_GMAME_PLAYERS(10111, AppId.CENTER),                   /// 这一局有哪几个人玩
	CENTER_CREATE_ZJH_DESK_SUCC(10112, AppId.CENTER),            //桌子创建成功
	CENTER_CREATE_GRAD_NIU_DESK_SUCC(10113, AppId.CENTER),         // 创建抢庄牛桌子成功
	CENTER_PLAYER_DESK_IS_REMOVE(10114, AppId.CENTER),             // logic通知center桌子已经解算了
	CENTER_GATE_FACTOR(10115, AppId.CENTER),                         // gate同步最新负载数
	CENTER_DEBUG_ARRAY_PAI_RES(10116, AppId.CENTER),             // 设置排牌返回
	CENTER_DEBUG_CHECK_FAN_TYPE_RES(10117, AppId.CENTER),        // 檢測牌型返回

	COUPLE_MONEY_CHANGE(10300),                                 //钱币修改
	LOGIC_CREATE_DESK(10301),
	LOGIC_ENTER_NIUNIU_DESK(10302),                              //进入牛牛房间
	LOGIC_PLAYER_LEAVE_DESK(10303),                              //玩家离开桌子
	LOGIC_CREATE_ROOM_DESK(10304),                               //玩家创建房间麻将
	LOGIC_DISBAND_ROOM_DESK(10305),                              //解散好友私房
	LOGIC_UPDATE_MONEY(10306),                                   //更新货币
	LOGIC_RELOAD_DYNAMIC(10307),                                 //通知logic修改配置
	LOGIC_RELOAD_CONF_ROOM(10308),                               //通知logic修改配置
	LOGIC_RELOAD_CONF_PERSONAL_ROOM(10309),                      //通知logic修改私房的配置
	LOGIC_REMOVE_DESK(10310),                                    //强制刪除桌子
	LOGIC_REMOVE_SERVER(10311),                                  //服务器要关闭
	LOGIC_ENTER_GRAB_NIU_DESK(10312),                             // 进入抢庄牛房间
	LOGIC_DEBUG_ARRAY_PAI(10313),                                 // 通知logic修改排牌配置
	LOGIC_DEBUG_CHECK_FAN_TYPE(10314),                             // 通知logic檢測牌型

	GATE_BROAD_CAST_MESSAGE(10500, AppId.GATE),                  //广播消
	GATE_KICK_PLAYER(10501, AppId.GATE),                         //剔玩家下线
	GATE_KICK_ALL_PLAYER(10502, AppId.GATE),                     //剔玩家下线
	GATE_REMOVE_SERVER(10503, AppId.GATE),                       //服务器要关闭

	LOG_ACCOUNT(10600, AppId.LOG),                               //写日志
	LOG_MONEY(10601, AppId.LOG),                                 //金币明细
	LOG_ONLINE(10602, AppId.LOG),                                //金币明细
	LOG_BANK(10603, AppId.LOG),                                  //银行记录
	LOG_REMOVE_SERVER(10604, AppId.LOG),                         //服务器要关闭

	LOGIN_RELOAD_CONF(10700, AppId.LOGIN),                         // 重载登陆服数据
	LOGIN_REMOVE_SERVER(10701, AppId.LOGIN),                     //服务器要关闭


	LOGIC_PLAYER_LOGIN(0x1001, AppId.LOGIC),;

	private final AppId sendTo;

	RequestCode(int value, AppId sendTo) {
		this.sendTo = sendTo;
		this.value = value;
	}

	RequestCode(int value) {
		this.value = value;
		this.sendTo = AppId.LOGIC;
	}

	public static List<RequestCode> NO_NEED_LOG_CODE_LIST = Arrays.asList(RESPONSE_TO_CLIIENT_CODE, ACCOUNT_GET_DYNAMIC_CONFIG);

	public int getValue() {
		return this.value;
	}

	public AppId getSendTo() {
		return sendTo;
	}

	private int value;

	private static Map<Integer, RequestCode> idCaches = new ConcurrentHashMap<>();

	public static RequestCode getByValue(int value) {
		RequestCode cacheCode = idCaches.get(value);
		if (cacheCode != null) {
			return cacheCode;
		}
		for (RequestCode code : values()) {
			if (code.getValue() == value) {
				idCaches.put(value, code);
				return code;
			}
		}
		return RequestCode.RESPONSE_TO_CLIIENT_CODE;
	}
}
