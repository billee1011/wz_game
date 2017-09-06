package network.handler.module;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import actor.CenterActorManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import common.LogHelper;
import config.bean.AgentInfoData;
import config.bean.ChannelConfig;
import config.bean.Province;
import config.provider.AgentInfoProvider;
import config.provider.ChannelInfoProvider;
import data.AccountAction;
import data.MoneySubAction;
import database.DBUtil;
import database.DataQueryResult;
import define.AppId;
import define.Gender;
import define.constant.CodeValidConst;
import define.constant.MessageConst;
import io.netty.channel.ChannelHandlerContext;
import logic.name.PlayerNameManager;
import net.sf.json.JSONObject;
import network.AbstractHandlers;
import network.ServerManager;
import packet.CocoPacket;
import protobuf.Account;
import protobuf.Common;
import protobuf.creator.AccountCreator;
import protobuf.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.ASObject;
import util.MiscUtil;

/**
 * Created by think on 2017/4/11.
 */
public class AccountModule implements IModuleMessageHandler {
	private static Logger logger = LoggerFactory.getLogger(AccountModule.class);

	private static AtomicInteger order_id = new AtomicInteger(1);

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.ACCOUNT_REGISTER.getValue(), this::actionRegister, Account.PBRegisterReq.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_BIND_ALI_PAY.getValue(), this::actionBindAliPay, Account.PBBindAliPayReq.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_MODIFY_BANK_PASSWORD.getValue(), this::actionModifyBankPassword, Account.PBModifyBankPasswordReq.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_SAVE_MONEY.getValue(), this::actionSaveMoney, Account.PBSaveMoneyReq.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_WITHDRAW_MONEY.getValue(), this::actionWithdrawMoney, Account.PBWithdrawMoneyReq.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_MODIFY_ICON.getValue(), this::actionModifyIcon, Account.PBModifyHeadIconReq.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_UPDATE_DATA.getValue(), this::actionUpdateData);
		handler.registerAction(RequestCode.ACCOUNT_MODIFY_GENDER.getValue(), this::actionModifyGender);
//		handler.registerAction(RequestCode.ACCOUNT_REGISTER_GAME_ID.getValue(), this::actionRegisterGameId);     never used
		handler.registerAction(RequestCode.ACCOUNT_MODIFY_NICK_NAME.getValue(), this::actionModifyNickname, Common.PBString.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_GET_DYNAMIC_CONFIG.getValue(), this::actionGetDynamicConfig);
		handler.registerAction(RequestCode.ACCOUNT_GENE_PAY_ORDER.getValue(), this::actionGenePayOrder, Common.PBIntString.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_ADD_COIN.getValue(), this::actionAddCoin, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_GET_RANDOM_NAME.getValue(), this::actionGetRandomName);
		handler.registerAction(RequestCode.ACCOUNT_COMPLAINT.getValue(), this::actionComplaint, Account.PBComplaintReq.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_OPEN_BANK.getValue(), this::openBank);
	}
	
	private void openBank(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		player.setShowBankMark(0);
	}
	
	private void actionComplaint(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		Account.PBComplaintReq request = message.get();
		int type = request.getType();
		String url = "";
		Map<String, String> params = new HashMap<>();
		params.put("platform_id", "3"); // 麻将组为3
		params.put("type", String.valueOf(type)); 
		params.put("content", request.getText()); 
		params.put("userid", String.valueOf(player.getPlayerId())); 
		if (type == 1) { // 投诉代理
			AgentInfoData agentDate = AgentInfoProvider.getInst().getAgentInfoForWx(request.getWeixin(),
					player.getPlatform_id());
			if (agentDate == null) { // 提示无效的代理
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.COMPLAINT_AGENT_ERROR));
				return; 
			}
			url = CenterServer.getInst().getComplaint_agent_url();
			params.put("agent_id", String.valueOf(agentDate.getAgent_id()));
			params.put("agent_nickname", String.valueOf(agentDate.getAgent_name()));
			params.put("wechat", String.valueOf(agentDate.getWeixin()));
		} else { // 投诉客服
			url = CenterServer.getInst().getComplaint_cs_url();
		} 
		String urlSend = url;
		CenterActorManager.getHttpActor().put(() -> http.HttpUtil.sendPost(urlSend, params), e -> {
			logger.info("请求投诉 codeUrl {} 参数{} 后回调 {}", urlSend, params.toString(), e);
			JSONObject res = JSONObject.fromObject(e);
			int code = res.getInt("code");
			if (code == 200) { // 投诉成功
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.COMPLAINT_SUC));
				return;
			} else if (code == 1002) { // 无法重复投诉
				if (type == 1) {
					player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.COMPLAINT_AGENT_LIMIT));
					return;
				} else {
					player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.COMPLAINT_KEFU_LIMIT));
					return;
				}
			} else {  // 1001 参数错误      1003 提交异常错误
				logger.error("请求投诉参数错误");
			}
		}, CenterActorManager.getLogicActor(player.getPlayerId()));
	}

	private void actionGetRandomName(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		player.write(ResponseCode.ACCOUNT_RANDOM_NAME
				, CommonCreator.createPBStringList(PlayerNameManager.getInst().random20NameForClient()));
	}

	private void actionAddCoin(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		ChannelConfig conf = ChannelInfoProvider.getInst().getChannelConfig(player.getChannelId(), player.getPackageId(), player.isReview());
		if (conf == null) {
			return;
		}
		if (conf.isRecharge()) {
			return;
		}
		if (!player.isReviewRechange()){ // 等于审核版本才能调试充钱 
			return;	
		}
		
		Common.PBInt32 request = message.get();
		int coin = request.getValue();
		player.updateCoin(coin * 100, true);
		PlayerSaver.savePlayerBase(player);
		logger.info("金币更新 :玩家 {} 渠道调试充值， 金币增加 {}，当前总金币数为 {}", player.getPlayerId(), coin * 100, player.getCoin());
		player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.ACTION_ADD_COIN));
		player.write(ResponseCode.ACCOUNT_UPDATE_DATA, AccountCreator.createPBLoginSucc(player));
	}

	private void actionGenePayOrder(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBIntString request = message.get();
		String plat = request.getValue();
		int chargeNum = request.getKey(); // 客户端传的单位为分
		if (chargeNum < 1000 || chargeNum % 10 != 0) {  // 必须大于10块钱且为10的倍数
			logger.error("充值金额小于 10 块 或是不是10的整数倍");
			return;
		}
		Province province = player.getProvinceData();
		// 区域限制
		if ((plat.equals("alipay") && !province.getAlipay()) || (plat.equals("wx") && !province.getWeixin())
				|| (plat.equals("unionpay") && !province.getUnionpay())) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PROVINCE_CLOSE));
			logger.error("无法充值  {}充值开关为关闭状态", plat);
			return;
		}
		
		String orderId = geneOrderId();
		StringBuilder builder = new StringBuilder();
		builder.append(CenterServer.getInst().getPayUrl());
		builder.append("?order_id=");
		builder.append(orderId);
		builder.append("&player_id=");
		builder.append(String.valueOf(player.getPlayerId()));
		builder.append("&charge_num=");
		builder.append(String.valueOf(chargeNum));
		builder.append("&phone_num=");
		builder.append(player.getPhone_num());
		builder.append("&paytype=");
		builder.append(plat);
		builder.append("&channel_id=");
		builder.append(String.valueOf(player.getChannelId()));
		builder.append("&device=");
		builder.append(player.getDevice());
		builder.append("&province=");
		builder.append(player.getProvince());
		builder.append("&ip=");
		builder.append(player.getIp());
		builder.append("&pre_coin=");
		builder.append(player.getCoin());
		builder.append("&pre_bank_coin=");
		builder.append(player.getBankMoney());
		builder.append("&package_id=");
		builder.append(player.getPackageId());
		builder.append("&provinceId=");
		builder.append(player.getProvinceData().getId());
		builder.append("&alipay_name=");
		builder.append(player.getAlipayName());
		builder.append("&deviceid=");
		builder.append(player.getMachine_id());
		logger.info("充值 ##### 发送给后台的数据:{}", builder.toString());
		CenterActorManager.getHttpActor().put(() -> http.HttpUtil.sendGet(builder.toString()));
	}

	private static String geneOrderId() {
		long currentSeconds = ((long) MiscUtil.getCurrentSeconds()) << 16;
		return String.valueOf(currentSeconds + order_id.getAndIncrement() % 10000);
	}

	private void actionGetDynamicConfig(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}

		player.write(ResponseCode.ACCOUNT_DYNAMIC_CONFIG, CenterServer.getInst().createPBDynamicConfig(player.getChannelId(), player.getPackageId(), player));
	}

	private void actionModifyNickname(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBString req = message.get();
		String nickName = req.getValue();
		if (!PlayerNameManager.getInst().isNickNameAvailable(nickName, player.getName())) {
			player.write(ResponseCode.ACCOUNT_MODIFY_NAME_SUCC, CommonCreator.createPBString(""));
			return;
		}
		player.setName(nickName);
		player.write(ResponseCode.ACCOUNT_MODIFY_NAME_SUCC, CommonCreator.createPBString(nickName));
	}

	private void actionUpdateData(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		player.write(ResponseCode.ACCOUNT_UPDATE_DATA, AccountCreator.createPBLoginSucc(player));
	}

	private void actionModifyIcon(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Account.PBModifyHeadIconReq request = message.get();
		player.setIcon(request.getIcon());
		player.setGender(Gender.getByValue(request.getGender()));
		player.write(ResponseCode.ACCOUNT_MODIFY_ICON, AccountCreator.createPBLoginSucc(player));
	}

	private void actionModifyGender(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		player.switchGender();
		player.write(ResponseCode.ACCOUNT_MODIFY_GENDER, AccountCreator.createPBLoginSucc(player));
	}

	private void actionSaveMoney(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		if (player.getDeskInfo() != null) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.AT_GAME_ROOM_ING_SAVE_MONEY));
			return;
		}
		Account.PBSaveMoneyReq request = message.get();
		long coinValue = Long.parseLong(request.getMoneyValue());
		if (coinValue <= 0) {
			return;
		}
		if (player.getCoin() < coinValue) {
			return;
		}
		long pre_coin = player.getCoin();
		long pre_bank_coin = player.getBankMoney();
		player.updateCoin(coinValue, false);
		player.updateBankCoin(coinValue, true);
		PlayerSaver.savePlayerBase(player);
		logger.info("銀行更新 :玩家 {} 存钱， 銀行增加 {}，当前总金币数为 {}， 银行存款为 {}", player.getPlayerId(), coinValue, player.getCoin(), player.getBankMoney());
		logger.info("金币更新 :玩家 {} 存钱， 金币减少 {}，当前总金币数为 {}， 银行存款为 {}", player.getPlayerId(), coinValue, player.getCoin(), player.getBankMoney());
		ServerManager.getInst().getMinLoadSession(AppId.LOG)
				.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
						, LogHelper.logLoseMoney(player.getPlayerId(), MoneySubAction.SAVE_LOSE.getValue(), 0, (int) coinValue, pre_coin, pre_coin-coinValue, player.getIp(), player.getChannelId(), String.valueOf(player.getPackageId()), player.getDevice(), 0)));
		ServerManager.getInst().getMinLoadSession(AppId.LOG)
				.sendRequest(new CocoPacket(RequestCode.LOG_BANK
						, LogHelper.logBankSave(player.getPlayerId(), (int) coinValue, (int) player.getCoin(), (int) player.getBankMoney(), pre_bank_coin, player.getIp(), player.getChannelId(), String.valueOf(player.getPackageId()), player.getDevice())));
		player.write(ResponseCode.ACCOUNT_SAVE_MONEY, AccountCreator.createPBLoginSucc(player));
	}

	private void actionWithdrawMoney(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Account.PBWithdrawMoneyReq request = message.get();
		if (!player.getBankPassword().equals(request.getBankPassword())) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.BANK_PASSWORD_ERROR));
			return;
		}
		long withDrawMoney = Long.parseLong(request.getMoneyValue());
		if (withDrawMoney <= 0) {
			return;
		}
		if (withDrawMoney > player.getBankMoney()) {
			return;
		}
		long pre_coin = player.getCoin();
		player.updateBankCoin(withDrawMoney, false);
		player.updateCoin(withDrawMoney, true);
		PlayerSaver.savePlayerBase(player);
		logger.info("銀行更新 :玩家 {} 取钱， 銀行減少 {}，当前总金币数为 {}， 银行存款为 {}", player.getPlayerId(), withDrawMoney, player.getCoin(), player.getBankMoney());
		logger.info("金币更新 :玩家 {} 取钱， 金币增加 {}，当前总金币数为 {}， 银行存款为 {}", player.getPlayerId(), withDrawMoney, player.getCoin(), player.getBankMoney());
		if (player.getDeskInfo() != null) {
			player.getDeskInfo().writeToLogic(new CocoPacket(RequestCode.LOGIC_UPDATE_MONEY
					, CommonCreator.createPBInt32((int) player.getCoin()), player.getPlayerId()));
		}
		long pre_bank_coin = player.getBankMoney();
		ServerManager.getInst().getMinLoadSession(AppId.LOG)
				.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
						, LogHelper.logGainMoney(player.getPlayerId(), MoneySubAction.WITHDRAW_GAIN.getValue(), 0, (int) withDrawMoney, pre_coin, pre_coin+withDrawMoney, player.getIp(), player.getChannelId(), String.valueOf(player.getPackageId()), player.getDevice(), 0)));
		ServerManager.getInst().getMinLoadSession(AppId.LOG)
				.sendRequest(new CocoPacket(RequestCode.LOG_BANK
						, LogHelper.logBankWithdraw(player.getPlayerId(), (int) withDrawMoney, (int) player.getCoin(), (int) player.getBankMoney(), pre_bank_coin, player.getIp(), player.getChannelId(), String.valueOf(player.getPackageId()), player.getDevice())));
		player.write(ResponseCode.ACCOUNT_WITHDRAW_MONEY, AccountCreator.createPBLoginSucc(player));
	}
	
	private int checkValidCode(String phoneNum,int code){
		if (null == phoneNum || 0 == phoneNum.length()) {
			logger.error("checkValidCode phoneNum error:" + phoneNum);
			return 4;
		}
		List<ASObject> codeList = DataQueryResult.load("SELECT id,valid_code,`status`,create_time FROM valid_code WHERE id = (SELECT MAX(id) FROM valid_code WHERE phone_num = '"+phoneNum+"')");
		if(codeList != null && codeList.size() > 0){
			ASObject obj = codeList.get(0);
			int validCode = obj.getInt("valid_code");
			int geneTime = obj.getInt("create_time");
			
			if (validCode != code) {
				logger.error("actionRegister failed_1 -> info.validCode:{}", validCode);
				return 1;
			}
			if (MiscUtil.getCurrentSeconds() - geneTime > CodeValidConst.EXPIRE_TIME) {
				logger.error("actionRegister failed_2 -> info.geneTime:{}", geneTime);
				return 2;
			}
			return 0;
		}
		return 3;
	}

	private void actionRegister(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Account.PBRegisterReq request = message.get();
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
        logger.info("玩家 {} 升级账号,nickName 为 {}", packet.getPlayerId(), player == null ? "null" : player.getName());
        if (player == null) {
			return;
		}
        
        // 区域控制
        if (!player.getProvinceData().getAccountUp()) {
        	player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PROVINCE_CLOSE));
        	logger.error("无法升级账号 {}区域升级账号开关为关闭状态", player.getProvince());
        	return;
        }

		/// 先检查该账号是否绑定手机
		if (0 < player.getPhone_num().length()) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PLAYER_PHONE_HAS_REGISTER));
			return;
		}

		/// 没有绑定手机再检查该手机号码是否被绑定
		if (accountAvailable(request.getPhoneNum(), player.getPlatform_id())) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.PHONE_HAS_USED));
			return;
		}
		
		int state = checkValidCode(request.getPhoneNum(),Integer.parseInt(request.getValidNum()));
		switch (state) {
		case 0:
			player.setIcon(request.getHeadIcon());
			player.setPhone_num(request.getPhoneNum());
			Map<String, Object> data = new HashMap<>();
			data.put("phone_num", request.getPhoneNum());
			data.put("password", request.getPassword());
			data.put("register_time", MiscUtil.getCurrentSeconds());
			Map<String, Object> where = new HashMap<>();
			where.put("user_id", player.getAccountId());
			CenterActorManager.getDbActor(player.getPlayerId()).put(() -> {
				try {
					DBUtil.executeUpdate("accounts", where, data);
				} catch (SQLException ex) {
					logger.error(" exception : {}", ex);
				}
				return null;
			});
			ServerManager.getInst().getMinLoadSession(AppId.LOG).sendRequest(new CocoPacket(RequestCode.LOG_ACCOUNT
					, LogHelper.logAccount(player.getAccountId(), MiscUtil.getCurrentSeconds(), player.getChannelId(), AccountAction.REGISTER.getValue()
					, player.getPackageId(), player.getIp(), player.getProvince(), player.getCity(), player.getDevice())));

			long pre_coin = player.getCoin();
			player.updateCoin(CenterServer.getInst().getUpgradeCoin(), true);
			PlayerSaver.savePlayerBase(player);
			logger.info("金币更新 :玩家 {} 升级账号， 金币增加 {}，当前总金币数为 {}", player.getPlayerId(), CenterServer.getInst().getUpgradeCoin(), player.getCoin());
			ServerManager.getInst().getMinLoadSession(AppId.LOG)
					.sendRequest(new CocoPacket(RequestCode.LOG_MONEY
							, LogHelper.logGainMoney(player.getPlayerId(), MoneySubAction.UPGRADE_GAIN.getValue(), 0, CenterServer.getInst().getUpgradeCoin(), pre_coin, player.getCoin(), player.getIp(), player.getChannelId(), String.valueOf(player.getPackageId()), player.getDevice(), 0)));

			//修改成功, 玩家下次登录的时候需要账号密码了, 这个机器码作废
			player.write(ResponseCode.ACCOUNT_RIGISTER_SUCC, AccountCreator.createPBLoginSucc(player));
			player.write(ResponseCode.ACCOUNT_MODIFY_PASSWORD, CommonCreator.createPBString(request.getPassword()));
			break;
		case 2:
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.REGISTER_TIME_OUT));
			break;
		default:
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.CODE_ERROR));
			break;
		}
	}

	private boolean accountAvailable(String phone_num, String platform_id) {
		Map<String, Object> phoneWhere = new HashMap<>();
		phoneWhere.put("phone_num", phone_num);
		phoneWhere.put("platform_id", platform_id);
		try {
			int count = DBUtil.executeCount("accounts", phoneWhere);
			if (count > 0) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void actionBindAliPay(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Account.PBBindAliPayReq request = message.get();
		Map<String, Object> where = new HashMap<>();
		where.put("alipay_account", request.getAliAccount());
		where.put("platform_id", player.getPlatform_id());
		
		CenterActorManager.getDbActor(player.getPlayerId()).put(() -> DataQueryResult.load("accounts", where), g -> {
			List<ASObject> result = (List<ASObject>) g;
			if (result.size() > 0) {
				player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.ALIPAY_HAS_BIND));
				return;
			}
			where.clear();
			where.put("user_id", player.getAccountId());
			Map<String, Object> data = new HashMap<>();
			data.put("alipay_account", request.getAliAccount());
			data.put("alipay_name", request.getBindingName());
			CenterActorManager.getDbActor(player.getPlayerId()).put(() -> {
				try {
					DBUtil.executeUpdate("accounts", where, data);
				} catch (SQLException e) {
					e.printStackTrace();
					return null;
				}
				return null;
			});
			player.setAlipayAccount(request.getAliAccount());
			player.setAlipayName(request.getBindingName());
			player.write(ResponseCode.ACCOUNT_BIND_ALI_PAY, AccountCreator.createPBLoginSucc(player));

		}, CenterActorManager.getLogicActor(player.getPlayerId()));
	}


	private void actionModifyBankPassword(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Account.PBModifyBankPasswordReq request = message.get();
		if (!request.getOriginalPassword().equals(player.getBankPassword())) {
			player.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.ORIGINAL_PASSWORD_ERROR));
			return;
		}
		player.setBankPassword(request.getNewPassword());
		player.write(ResponseCode.ACCOUNT_MODIFY_BANK_SUCC, null);
	}
	

}
