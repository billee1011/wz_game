
/********************************************************************
	created:	2014/08/05   15:42
	filename: 	ClientEventType.cs
	author:		W_X
	purpose:	
*********************************************************************/

namespace P3GameClient
{
	///<summary>
	///GameeventI.
	///</summary>
	public enum GameEventID
	{
		GEIdUndefined = -1,

		GEShowUIWindow = 1,// 打开UI界面 参数: 1.window名 2.args;
		GERequestChatLink = 2, // 请求聊天链接 参数: 1.链接类型 2.发链接的玩家Id 3.链接ID;
		GECloseUIWindow = 3, // 关闭UI界面 参数 1.window名;
		// 禁止在上面增加非触发事件消息;
		//////////////////////////////////////////////////////////////////////////

		GEDynamicHudPos_Add,			//HUDText事件
		GEDynamicHudTran_Add,

		///<summary>HUD图片事件</summary>
		GEHudSpriteTran_Add,

		///<summary>HUD图片移除</summary>
		GEHudSprite_Remove,

		GEHudDialogue,// hud对话;

		GEDynamicScrollItem_Add,		//ScrollView 列表事件

		///<summary>主界面钱币显示</summary>
		GEMainUIShow,

		#region UISelectServer
		GESelectServerChange,
		#endregion

		#region		UIBattle
		/// <summary>
		/// 添加血量,怒气条
		/// </summary>
		GEBattleAddSlot,

		/// <summary>
		/// 移除血量,怒气条
		/// </summary>
		GEBattleRemoveSlot,

		///<summary>设置血条的状态</summary>
		GEBattleSetSlotActive,

		/// <summary>
		/// 血量
		/// </summary>
		GEBattleChangeHP,

		/// <summary>
		/// 显示回合数或者战斗波数;
		/// </summary>
		GEBattleRoundOrGroupActive,

		/// <summary>
		/// 改变回合数
		/// </summary>
		GEBattleRoundChange,

		/// <summary>
		/// 怪物波数显示
		/// </summary>
		GEBattleGroupChange,

		/// <summary>
		/// 显示继续按钮
		/// </summary>
		GEBattleShowGoonButton,

		///<summary>显示UIQTE特效</summary>
		GEBattleShowQTEEffect,

		///<summary>每一波战斗结束</summary>
		GEBattleGroupEnd,

		///<summary>合击事件</summary>
		GEBattleQTECombo,

		///<summary>物品掉落</summary>
		GEBattleFallDownItem,

		///<summary>英雄眩晕</summary>
		GEBattleSwim,

		/// <summary>
		/// 给提示框添加内容
		/// </summary>
		GEAddDialogueText,

		/// <summary>
		/// 副本通关信息有变化;
		/// </summary>
		GELevelInfoChange,

		/// <summary>
		/// 所选关卡改变
		/// </summary>
		GELevelSelectChange,

		/// <summary>
		/// 玩家属性变更;
		/// </summary>
		GEPlayerInfoChange,
		GEPlayerVipLvlUp,//VIP等级提升

		/// <summary>
		/// 玩家英雄属性变更
		/// </summary>
		GEPlayerHeroInfoChange,

		///<summary>显示合击伤害</summary>
		GEBattleShowComboHurt,

		///<summary>显示英雄仙缘组合</summary>
		GEBattleShowComboTeam,
		#endregion

		#region 物品相关
		/// <summary>
		/// 物品变更
		/// </summary>
		GEItemChange,
		/// <summary>
		/// 物品出售
		/// </summary>
		GEItemSell,
		#endregion

		#region 阵法相关
		/// <summary>
		/// 阵法变更
		/// </summary>
		GEMatrixChange,
		#endregion

		GELeaveLoginState,	// 离开登录状态;
		GEEnterPVEBattleState,		// 进入PVE副本;
		GEEnterMainGameState,	// 进入主界面;

		GEHeroDataChange,// 英雄属性改变;

		GEGuideTargetChange,// 新手指引需要点击的按钮改变;
		GERandomNickNamesChange,

		#region 法宝相关
		GEMagicWeaponLevelUp,// 法宝境界升级;
		GEMagicEyeLevelUp,// 法眼升级;

		GEMagicWeaponSmeltResultChange,// 洗练的属性浮动值改变;
		GESmeltHeroChange,// 切换洗练的英雄;

		#endregion

		#region 英雄界面相关
		GEHeroCardChange,		// 英雄改变;
		GEHeroNewSkill,			// 解锁新技能;
		GEHeroNew,				//新增英雄;
		GEHeroEquipRefreshUI,	//装备UI刷新
		GEHeroEquipLvlUp,	//装备进阶成功
		GEHeroStarLvlUp,	//英雄升星成功
		GEChangeSelect,		//更改选择的英雄
		#endregion

		#region 商店相关
		///<summary>商店刷新</summary>
		GEShopRefresh,

		///<summary>购买物品</summary>
		GEShopItemBuy,
		#endregion

		#region 兑换相关
		///<summary>兑换金钱</summary>
		GEConvertMoney,

		///<summary>兑换体力</summary>
		GEConvertPower,

		///<summary>显示兑换结果</summary>
		GEConvertShowResult,
		#endregion

		#region 抽卡相关
		GELotteryStatusChange,// 抽卡相关数据改变;
		GELotteryBeginRewardAnima,// 抽卡奖励动画开始;
		GERefreshDraw,				//刷新抽卡界面
		#endregion

		#region 邮件相关
		GENewMail,		// 新邮件;
		GEMailDescrib,	// 邮件详细内容
		GEMailGetAnnex,	// 邮件附件领取
		#endregion

		#region 聊天相关
		GEAddChat,// 新聊天内容;
		GESwitchPrivateChat,// 切换私聊;
		///<summary>服务器GM帮助</summary>
		GESeverInfo,
		#endregion

		#region 竞技相关
		GEPvpArmychange,// 竞技对手更换;
		GEPvpBaseInfoChange,
		#endregion

		#region 排行榜相关
		GERankRefresh,		// 排行榜刷新;
		GERankExtRefresh,	// 排行榜扩展内容刷新;
		GERankFightRefresh,	// 排行榜战力刷新;
		#endregion

		#region 任务相关
		GEMissionRefresh,		// 任务刷新
		GEMissionDiscribUI,		// 任务详情UI刷新
		GEActiveRefresh,		// 活跃度刷新
		GEActiveReward,			// 活跃度奖励领取成功
		#endregion

		///<summary>改变界面</summary>
		GEEightyChange,

		///<summary>抽奖</summary>
		GEEightyLotteryDraw,

		///<summary>领取逢九奖励</summary>
		GEEightyNineReward,

		#region 签到相关
		GESignSuccess,		// 签到成功
		#endregion

		#region 兑换码相关
		GERedeemSuccess,	// 兑换成功
		GERedeemFail,		// 兑换失败
		#endregion

		#region 宗派相关
		GESectListRefresh,			// 宗派列表刷新
		GESectInfoRefresh,			// 用户所属宗派信息刷新
		GESectFlagRefresh,			// 宗派旗帜刷新
		GESectJoinLstRefresh,		// 申请者列表刷新
		GESectLogRefresh,			// 宗派日志刷新
		GESectChangeSuccess,		// 宗派设置修改结果
		GESectDisband,				// 成功解散宗派
		GESectOut,					// 已经不在宗派
		GEShowMemMgrMenu,			// 显示成员管理菜单
		#endregion

		#region 提示相关
		GEPromptChange,	// 提示信息变化了;
		#endregion

#region 好友相关
		GEBlackListRemoveSuccess,
#endregion

#region 新版界面相关
		GELevelType_Change,	// 副本类型变更;
#endregion

#region 副本相关

		///<summary>进去副本</summary>
		GEEnterLevel,

		///<summary>刷新</summary>
		GERefreshChooseArmyUI,
#endregion

#region 设置界面相关
		///<summary>战斗头像和头像框变更</summary>
		GEChangeHeroIcon,
#endregion

		GEIdCount //禁止在这之后添加;
	}

}