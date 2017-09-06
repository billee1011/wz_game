package data;

/**
 * Created by think on 2017/3/21.
 */
public enum MoneySubAction {
	CHARGE_GAIN(1),					/// 充值
	EXCHANGE_LOSE(2),				/// 兑换
	WITHDRAW_GAIN(3),				/// 保险柜取出
	SAVE_LOSE(4),					/// 保险柜存入
	NIUNIU_LOSE(5),					/// 牛牛 输
	NIUNIU_GAIN(6),					/// 牛牛 贏
	COUPLE_MJ_GAIN(7),				/// 二人麻将 贏
	COUPLE_MJ_LOSE(8),				/// 二人麻将 输
	XUENIU_GAIN(9),					/// 血流 贏
	XUENIU_LOSE(10),				/// 血流 输
	XUEZHAN_GAIN(11),				/// 血战 贏
	XUEZHAN_LOSE(12),				/// 血战 输
	WEB_UPDATE(13),					/// 后台加钱
	AGENT_ADD(14),					/// 代理加钱
	AGENT_LOSE(15),					/// 代理扣钱
	UPGRADE_GAIN(16),				/// 升级账号金币
	RANK_GAIN(17),					/// 排名奖励
	EXCHANGE_RESTORE(18),			/// 兑换补给
	ZJH_WIN(19),					/// 炸金花 贏
	ZJH_LOSE(20),					/// 炸金花 输
	DDZ_WIN(21),					/// 斗地主 贏
	DDZ_LOSE(22),					/// 斗地主 输
	DDZ_COUPLE_WIN(23),				/// 二人斗地主 贏
	DDZ_COUPLE_LOSE(24),			/// 二人斗地主 输
	DDZ_LZ_WIN(25),					/// 癩子斗地主 贏
	DDZ_LZ_LOSE(26),				/// 癩子斗地主 输
	GRAD_NIU_WIN(27),				/// 抢庄牛赢
	GRAD_NIU_LOST(28),				/// 抢庄牛牛输
	CLASS_NIU_WIN(29),				/// 经典牛赢
	CLASS_NIU_LOST(30),				/// 经典牛牛输
	AGENT_WITHDRAW(31),             /// 代理充错退款
	
	// 私房流水
	XUENIU_GAIN_PERSONAL(109),				/// 血流 贏
	XUENIU_LOSE_PERSONAL(110),				/// 血流 输
	XUEZHAN_GAIN_PERSONAL(111),				/// 血战 贏
	XUEZHAN_LOSE_PERSONAL(112),				/// 血战 输
	ZJH_WIN_PERSONAL(119),					/// 炸金花 贏
	ZJH_LOSE_PERSONAL(120),					/// 炸金花 输
	GRAD_NIU_WIN_PERSONAL(127),				/// 抢庄牛赢
	GRAD_NIU_LOST_PERSONAL(128),			/// 抢庄牛牛输
	CLASS_NIU_WIN_PERSONAL(129),			/// 经典牛赢
	CLASS_NIU_LOST_PERSONAL(130),			/// 经典牛牛输

	;


	private int value;

	MoneySubAction(int value) {
		this.value = value;
	}

	public int getValue(){
		return this.value;
	}
}
