package protocol.s2c;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ResponseCode {
    PONG(30002),

    ACCOUNT_LOGIN_SUCC(30101),
    ACCOUNT_TEST_REPLY(30102),
    ACCOUNT_MODIFY_GENDER(31004),
    ACCOUNT_MODIFY_PASSWORD(31005),
    ACCOUNT_SAVE_MONEY(31006),
    ACCOUNT_WITHDRAW_MONEY(31007),
    ACCOUNT_RETRIEVE_PASS(31008),
    ACCOUNT_BIND_ALI_PAY(31009),
    ACCOUNT_MODIFY_BANK_SUCC(31010),
    ACCOUNT_EXCHANGE_SUCC(31011),
    ACCOUNT_LOGIN_FAILED(31012),                    //登录失败
    ACCOUNT_VALID_NUM(31013),                       //获取验证码
    ACCOUNT_WX_PRE_ID(31014),                       //返回微信支付吗
    ACCOUNT_CHARGE_SUCC(31015),                      //
    ACCOUNT_UPDATE_DATA(31016),                      //
    ACCOUNT_GENE_ORDER(31017),                      //生成订单
    ACCOUNT_LOGIN_OTHER_WHERE(31018),              //别处登录
    ACCOUNT_GAMING(31019),                        //在游戏中不能进行兑换跟存钱操作;
    ACCOUNT_UPDATE_COIN(31020),                        //刷新钱币;
    ACCOUNT_MODIFY_NAME_SUCC(31021),                    //修改昵称成功
    ACCOUNT_DYNAMIC_CONFIG(31022),                    //动态配置的信息
    ACCOUNT_MODIFY_RANK_CONFIG(31023),                    //排行榜奖励信息发生改变
    ACCOUNT_MODIFY_ROOM_CONF(31024),                        //房间配置信息改变
    ACCOUNT_UPDATE_DESK_COIN(31025),                  //一局结束时同步金币
    ACCOUNT_ANNOUNCEMENT(31026),                        //公告
    ACCOUNT_RANDOM_NAME(31027),                        //随机名
    ACCOUNT_MODIFY_AGENT_INFO(31028),                    //动态配置的代理信息
    ACCOUNT_MODIFY_RANKCOIN(31029),                    //保险箱金额修改
    ACCOUNT_TRANSFER_INFO(31030),                    //转账记录
    ACCOUNT_SHOW_BANK_MARK(31031),                   //添加保险箱标记

    //////////////////////////////////////////////////
    /////////////////////////////////////////////////
    LOBBY_GAME_ROOM_PLAYER_NUM(32001),
    LOBBY_RANK_INFO(32002),
    LOBBY_IN_GAME(32003),
    LOBBY_MACHING(32004),
    LOBBY_ENTER_MONERY_NOT_ENOUGH(32005),
    LOBBY_GET_ROOM_DESK_INFO(32006),
    LOBBY_TRANS_VOICE(32007),
    LOBBY_DISBAND_SUCC(32008),                        //成功解散游戏
    LOBBY_DISBAND_FAILED(32009),                        //解散失败
    LOBBY_PLAYER_WANT_DISBAND(32010),                //XX发起解散提议
    LOBBY_PLAYER_AGREE_DISBAND(32011),                //XX同意解散提议
    LOBBY_PLAYER_MODULE_NUM(32012),                    //在不同类型游戏方式中玩家的数量
    LOBBY_PLAYER_ENTER_MODULE_SUCC(32013),                    //进入游戏模块成功
    LOBBY_GET_ROOM_SCORE_LIST(32014),                        //获得好友房的战绩
    LOBBY_PAO_MA_DENG(32015),                                    //跑马灯
    LOBBY_NO_DESK(32016),                //桌子不存在
    LOBBY_GAME_RECORD(32017),    //游戏回放
    ZJH_ENTER_DESK(32018),   //玩家进入游戏
    LOBBY_PLAYER_LEAVE(32019),       //玩家离开游戏了 删除了
    LOBBY_TICK_MYSELF(32020),        // 踢自己         TODO------------------
    LOBBY_TICK_PLAYER(32021),     // 踢人     0：无消息   1：长时间没操作 踢出

    ///////////////////////////////////////////////
    ///////////////////////////////////////////////
    COUPLE_GAME_START(20400),                        //开局发牌
    COUPLE_DEAL_CARD(33001),                            //发牌
    COUPLE_GAME_READY(33002),                        //玩家准备
    COUPLE_ENTER_DESK(33003),                            //进桌
    COUPLE_DROP_CARD(33004),                        //出牌
    COUPLE_WANT_OPERATION(33005),                    //要操作
    COUPLE_CHI(33006),                                //吃牌
    COUPLE_KE(33007),                                //刻牌
    COUPLE_GANG(33008),                                //杠牌
    COUPLE_HU(33009),                                //胡牌
    COUPLE_DO_TING(33010),                            //发牌
    COUPLE_DEAL_ONE_CARD(33011),                    //摸牌
    COUPLE_GUO(33012),                                //过牌
    COUPLE_LIUJU(33013),                            //流局
    COUPLE_ON_TING(33014),
    COUPLE_OPPOSITE_LEAVE(33015),                    //玩家离开
    COUPLE_RESET_GAME(33016),                        //还原游戏数据
    COUPLE_PLAYER_ENTER_DESK(33017),                        //还原游戏数据
    COUPLE_MONEY_NOT_ENOUGH(33018),                           //不够钱进行准备
    COUPLE_UPDATE_DESK_MONEY(33019),                            //更新桌上玩家的钱
    COUPLE_DEL_VALUE(33020),                            // 删除牌

    MAIL_ALL_MAIL(34001),
    MAIL_NEW_MAIL(34002),
    MAIL_DELETE(34003),
    MAIL_CONTACT_KEFU(34004),
    MAIl_GAIN_REWARD_SUCC(34005),
    MAIl_READ_SUCC(34006),
    MAIL_ALL_KEHU_MESSAGE(34007),
    MAIL_KEFU_RESPONSE(34008),
    MAIL_RETURN_AGEENT_RESPONSE(34009),
    MAIL_RETURN_AGEENT_TRANSFER(34010),
    //////////////////////////////////////////////////////
    //////////////////////////////////////////////////////

    NIUNIU_CHIP_INFO(35001),
    NIUNIU_RESULT(35002),
    NIUNIU_CHIP_SUCC(35003),
    NIUNIU_HIS_RECORD(35004),
    NIUNIU_CHIP_UPDATE(35005),
    NIUNIU_ROB_BANNER_SUCC(35006),
    NIUNIU_BANNER_CHANGE(35007),
    NIUNIU_GIVE_UP_BANNER_SUCC(35008),
    NIUNIU_CANCEN_ROB_BANNER(35009),
    NIUNIU_RANK_INFO(35010),
    NIUNIU_RENEWED_SUCC(35011),

    //////////////////////////////////////////////
    XUENIU_BEGIN_SWAP(36001),
    XUENIU_BEGIN_CHOSE_TYPE(36002),
    XUENIU_CHOSE_TYPE_END(36003),
    XUENIU_GAME_END(36004),
    XUENIU_CALCULATE(36005),
    XUENIU_RENSHU(36006),                            //认输\
    XUENIU_OPERATION(36007),                          //钱不够了, 通知一下
    XUENIU_CONTINUE(36008),                          //血流继续游戏
    XUENIU_DISBAND_INFO(36009),                      //私房麻将解散信息
    XUENIU_HU_TOGETHER(36010),                       //一炮多响通知

    DDZ_DEAL_CARD(38001),                            //斗地主发牌
    DDZ_ROB_LORD(38002),                            //斗地主抢地主
    DDZ_DISCARD_TURN(38003),                        //斗地主轮到谁出牌
    DDZ_DISCARD_CARD(38004),                        //斗地主出牌
    DDZ_PASS(38005),                                //斗地主pass操作
    DDZ_LORD_CARD(38006),                            //斗地主亮庄家底三张
    DDZ_RESULT(38007),                                //斗地主结算
    DDZ_WHO_LORD(38008),                             //斗地主谁抢地主中
    DDZ_WHO_JIABEI(38009),                           //谁在加倍中
    DDZ_JIABEI(38010),                           //斗地主加倍
    DDZ_RESET_GAME(38011),                           //斗地主回复游戏
    DDZ_TUOGUAN(38012),                           //有人托管了
    DDZ_CANCEL_TUOGUAN(38013),                   //有人取消托管了
    DDZ_LIUJU(38014),                             //斗地主流局

    ROOM_DESK_GAME_END(20900),                     //游戏结束
    ROOM_SCORE_DETAIL(20901),                        //单局游戏详情
    ROOM_CREATE_SUCC(20902),                        //私房桌子创建成功
    MESSAGE(20903),                                //弹字
    MESSAGE_EX(20904),                             //弹字
    KICK_PLAYER(20905),                            //踢通知玩家
    MESSAGE_PARAME(20906),                         //弹字 带参数
    PLAYRE_CG(20907),                               //玩家CG行为提示
    DEBUG_ARRAY_PAI(20908),                        //  排牌返回
    DEBUG_CHECK_FAN_TYPE(20909),                   //  檢查牌型返回

    ZJH_DEAL_CARD(39001),                //扎金花发牌
    ZJH_ADD_GOLD(39002),                 //扎金花加注回复
    ZJH_SEE_CARD(39003),             //扎金花看牌回复
    ZJH_COMPARE_CARD(39004),         //比牌回复
    ZJH_RESULT(39005),               //最终结果、
    ZJH_SHOW_CARDS(39006),           //亮牌
    ZJH_GIVE_UP(39007),              //弃牌
    ZJH_FULL_PRESSURE(39008),              //压满回复
    ZJH_DESK_INFO(39009),        //房间信息
    ZJH_RESET_GAME(39010),       //恢复游戏数据
    ZJH_ALL_IN(39011),       //孤注一掷
    ZJH_DISMIS_DESK(39012),  //解散桌子
    ZJH_START_READY(39013),  //可以发送准备了
    ZJH_PLAYER_STATE(39014), //更改玩家状态    0 非旁观1旁观

    GRADNIU_SEND_CARD(41001),            //  抢庄牛牛开始发牌
    GRADNIU_DESK_INFO(41002),            //  抢庄牛牌局字典信息
    GRADNIU_STATE(41003),                //  抢庄牛牛状态
    GRADNIU_NOTIFY_ZHUANG(41004),        //  通知谁是庄
    GRADNIU_NOTIFY_BET(41005),            //  通知谁下注了多少
    GRADNIU_DEAL_CARD(41006),            //  获取自己的牌型
    GRADNIU_RESULT(41007),                //  获取抢庄牛牛结果
    GRADNIU_PLAYER_CAL(41008),            //  抢庄牛已经算牌完毕

    ;

    public static List<ResponseCode> NO_NEED_LOG_CODE_LIST = Arrays.asList(NIUNIU_CHIP_UPDATE);

    ResponseCode(int value) {
        this.value = value;
    }

    private static Map<Integer, ResponseCode> idCaches = new ConcurrentHashMap<>();

    public static ResponseCode getByValue(int value) {
        ResponseCode cacheCode = idCaches.get(value);
        if (cacheCode != null) {
            return cacheCode;
        }
        for (ResponseCode code : values()) {
            if (code.getValue() == value) {
                idCaches.put(value, code);
                return code;
            }
        }
        return null;
    }

    private int value;

    public int getValue() {
        return this.value;
    }
}
