package network.handler.module;

import actor.CenterActorManager;
import chr.Player;
import chr.PlayerManager;
import chr.PlayerSaver;
import com.google.protobuf.MessageLite;
import config.bean.AgentInfoData;
import config.bean.ChannelConfig;
import config.bean.TransferData;
import config.provider.AgentAuickReplyProvider;
import config.provider.AgentInfoProvider;
import config.provider.AnnouncementProvider;
import config.provider.ChannelInfoProvider;
import config.provider.DynamicPropertiesPublicProvider;
import database.DBUtil;
import db.ProcLogic;
import define.constant.DynamicPublicConst;
import define.constant.MessageConst;
import handle.CenterHandler;
import io.netty.channel.ChannelHandlerContext;
import mail.MailEntity;
import network.AbstractHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.creator.CommonCreator;
import protobuf.creator.MailCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.CenterServer;
import util.MiscUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by think on 2017/4/11.
 */
public class BackendModule implements IModuleMessageHandler {
	private static Logger logger = LoggerFactory.getLogger(BackendModule.class);

	@Override
	public void registerModuleHandler(AbstractHandlers handler) {
		handler.registerAction(RequestCode.MAIL_CONTACT_KEFU.getValue(), this::actionContackKefu, Common.PBPairString.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_REQUEST_AGENT_INFO.getValue(), this::actionRequestAgentInfo);
		handler.registerAction(RequestCode.MAIL_SEND_AGENT_MESSAGE.getValue(), this::actionSendAgentMessge, Common.PBIntString.getDefaultInstance());
		handler.registerAction(RequestCode.AGENT_TRANSFER_REQUEST.getValue(), this::actionAgentTransfer, Common.PBIntIntString.getDefaultInstance());
		handler.registerAction(RequestCode.AGENT_TRANSFER_MESSAGE.getValue(), this::actionAgentTransferMessage, Common.PBInt32.getDefaultInstance());
		handler.registerAction(RequestCode.ACCOUNT_GET_ANNOUNCEMENT.getValue(), this::actionGetAnnouncement);
		handler.registerAction(RequestCode.MAIL_GET_ALL_KEFU_MESSAGE.getValue(), this::actionGetAllKehuMessage);
	}

	private void actionGetAllKehuMessage(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Map<String, String> data = new HashMap<>();
		data.put("player_id", String.valueOf(player.getPlayerId()));                        //获取改玩家所有的信息,名字是否告诉你啊 还是id是 9999的是客服啊
		CenterActorManager.getHttpActor().put(() -> {
			http.HttpUtil.sendPost(CenterServer.getInst().getBackUrl() + "/service/getAllMessage", data, f ->
					player.write(ResponseCode.MAIL_ALL_KEHU_MESSAGE, CommonCreator.createPBString(f.toString())));
			return null;
		});
	}


	private void actionGetAnnouncement(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		String annoUrl = "";
		int annoId = 0;
		ChannelConfig conf = ChannelInfoProvider.getInst().getChannelConfig(player.getChannelId(), player.getPackageId(), player.isReview());
		if (conf != null){ // 优先读渠道包配置
			annoUrl = conf.getAnno_url();
			annoId = conf.getAnno_id();
		}
		if (annoUrl == null || annoUrl.equals("")) { 
			annoUrl = player.getProvinceData().getAnnoUrl();
		}
		if (annoId == 0) { 
			annoId = player.getProvinceData().getAnnoId();
		}
		player.write(ResponseCode.ACCOUNT_ANNOUNCEMENT, CommonCreator.createPBStringList(AnnouncementProvider.getInst().getData(annoId), annoUrl));
	}

	private void actionAgentTransferMessage(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		Common.PBInt32 request = message.get();
		int sel_cur_page = request.getValue();

		int page_num = CenterServer.getInst().getAgent_transfer_page_count();
		List<TransferData> player_list = player.getTransferDataList();
//		Iterator<TransferData> it = player_list.iterator();
//		while(it.hasNext()){ // 過濾類型
//			TransferData date = it.next();
//			if(1 != date.getType() && 2 != date.getType()) {
//				it.remove();;
//			}
//		}
		int count_page = page_num == 0 ? 0 : player_list.size() / page_num;
		if (0 < player_list.size() % CenterServer.getInst().getAgent_transfer_page_count()) {
			count_page += 1;
		}
		if (sel_cur_page > count_page) {
			return;
		}
		int start_page = (sel_cur_page - 1) * page_num;
		List<TransferData> list_data = new ArrayList<>();
		for (int loop = start_page; loop < start_page + page_num; loop++) {
			if (loop >= player_list.size()) {
				break;
			}
//			if(1 != player_list.get(loop).getType() && 2 != player_list.get(loop).getType()) {
//				continue;
//			}
			TransferData transfer_data = new TransferData();
			transfer_data.setId(player_list.get(loop).getId());
			transfer_data.setPlayer_out_id(player_list.get(loop).getPlayer_out_id());
			transfer_data.setPlayer_in_id(player_list.get(loop).getPlayer_in_id());
			transfer_data.setPlayer_in_name(player_list.get(loop).getPlayer_in_name());
			transfer_data.setAmount(player_list.get(loop).getAmount());
			if (player_list.get(loop).getPlayer_out_id() == player.getPlayerId()) {
				transfer_data.setType(1);
			} else {
				transfer_data.setType(2);
			}
			transfer_data.setTime(player_list.get(loop).getTime());

			list_data.add(transfer_data);
		}

		player.write(ResponseCode.ACCOUNT_TRANSFER_INFO, MailCreator.createPBTransferList(list_data, count_page, request.getValue()));
	}

	private void actionAgentTransfer(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player_out = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player_out == null) {
			return;
		}
		int now = MiscUtil.getCurrentSeconds();
		if (MiscUtil.getCurrentSeconds() - player_out.getAgentPayTime() < 10) { // 转账10秒防误
			player_out.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.TRANSFER_TIME_OUT));
			return;
		} 
		
		String platformId = player_out.getPlatform_id();
		/// 是否为游客账号
//		if (0 >= player_out.getPhone_num().length()) {
//			player_out.write(ResponseCode.MESSAGE
//					, CommonCreator.createPBInt32(MessageConst.VISITOR_NO_TRANSFER));
//			return;
//		}
		Common.PBIntIntString request = message.get();
		int player_id_in = request.getOne();
		Player player_int = PlayerManager.getInstance().getPlayerById(player_id_in);
		if (player_int == null){
			player_out.write(ResponseCode.MESSAGE
					, CommonCreator.createPBInt32(MessageConst.ERROR_TRANSFER_ID));
			return;
		}
		if (!platformId.equals(player_int.getPlatform_id())){ // 不为同一平台
			player_out.write(ResponseCode.MESSAGE
					, CommonCreator.createPBInt32(MessageConst.TRANSFER_DIFF_PLATFORM));
			return;
		}
		int amount = request.getTwo();
		String rank_pwd = request.getThree();
		
		if (amount < 0){
			return;
		}

		/// 判断代理商是否合法
		AgentInfoData agent_info_out = AgentInfoProvider.getInst().getAgentInfoPlayerId(player_out.getPlayerId(), platformId);
		AgentInfoData agent_info_in = AgentInfoProvider.getInst().getAgentInfoPlayerId(player_id_in, platformId);
		//  必须一人是代理 一人是平民
		if ((null == agent_info_out && null == agent_info_in) || (null != agent_info_out && null != agent_info_in)) {
			player_out.write(ResponseCode.MESSAGE, CommonCreator.createPBInt32(MessageConst.TRANSFER_IDENTITY_ERROR));
			return;
		}
		Player player_in = PlayerManager.getInstance().getPlayerById(player_id_in);
//		if (null == player_in) {
//			tmp_player = PlayerLoader.getOfflineInfo(player_id_in);
			if (null == player_in) {
				player_out.write(ResponseCode.MESSAGE
						, CommonCreator.createPBInt32(MessageConst.TRANSFER_IDENTITY_ERROR));
				return;
			}
//		}

		/// 保险箱密码
		if (false == player_out.getBankPassword().equals(rank_pwd)) {
			player_out.write(ResponseCode.MESSAGE
					, CommonCreator.createPBInt32(MessageConst.TRANSFER_PASSWORD_ERROR));
			return;
		}

		/// 是否有这个金额
		if (player_out.getBankMoney() < amount + CenterServer.getInst().getAgentPayRetain() * 100) {
			player_out.write(ResponseCode.MESSAGE
					, CommonCreator.createPBInt32(MessageConst.TRANSFER_COIN_NOT_ENOUGH));
			return;
		}
		
		int player_out_id = packet.getPlayerId();
		int type = 1; // 代理充值
		if (null != agent_info_in) {
			type = 2; // 代理购入
		}
		int typeA = type;

		String nick_name = "";
//		if (null != player_in) {
			nick_name = player_in.getName();
			player_in.write(ResponseCode.ACCOUNT_MODIFY_RANKCOIN
					, CommonCreator.createPBString(player_in.getBankMoney()+""));
//		} else {
//			player_in = PlayerLoader.getOfflineInfo(player_id_in);
//			nick_name = player_in.getName();
//		}
		
		final String nick_nameA = nick_name;

		/// 插入数据库
		Map<String, Object> map_data = new HashMap<>();
		map_data.put("platform_id", platformId);
		map_data.put("player_out_id", player_out_id);
		map_data.put("player_in_id", player_id_in);
		map_data.put("player_in_name", nick_name);
		map_data.put("amount", amount);
		map_data.put("type", type);
		map_data.put("time", MiscUtil.getCurrentSeconds());
		long id = 0;
		try {
			id = DBUtil.executeInsert("agent_pay", map_data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		long idA = id;

		/// 代理商转账
		ProcLogic.agentTransfer((int) id, player_out_id, player_id_in, amount, now, type, (e)->{
			int ret = (int) e;
			if (200 != ret) {
				player_out.write(ResponseCode.MESSAGE
						, CommonCreator.createPBInt32(ret));
				return;
			}
			player_out.write(ResponseCode.ACCOUNT_MODIFY_RANKCOIN
					, CommonCreator.createPBString(player_out.getBankMoney()+""));
			
			/// 代理发送邮件
			MailEntity mail = MailEntity.createMail(player_out.getPlayerId(), player_out.getAvailableMailId(), 6, 0, String.valueOf(amount / 100),
					nick_nameA, String.valueOf(player_id_in));
			CenterActorManager.getDbActor(player_out.getPlayerId()).put(() -> {
				MailEntity.insertMailIntoDataBase(mail);
				return null;
			});
			player_out.addMail(mail);
			if (typeA == 1 || typeA == 2) { // 只有類型 1 和 2 展現客戶端
				player_out.addTransferList(TransferData.createTransferData((int) idA, player_out_id, player_id_in,
						nick_nameA, amount, typeA, MiscUtil.getCurrentSeconds()));
			}
			player_out.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail));
			
			/// 购入者发送邮件
			MailEntity mail1 = MailEntity.createMail(player_id_in, player_in.getAvailableMailId(), 7, 0, player_out.getName(), String.valueOf(player_out.getPlayerId()),
					String.valueOf(amount / 100));
			CenterActorManager.getDbActor(player_id_in).put(() -> {
				MailEntity.insertMailIntoDataBase(mail1);
				return null;
			});
			if (null != player_in) {
				player_in.addMail(mail1);
				player_in.addTransferList(TransferData.createTransferData((int) idA, player_out_id, player_id_in, nick_nameA, amount, typeA, MiscUtil.getCurrentSeconds()));
				player_in.write(ResponseCode.MAIL_NEW_MAIL, MailCreator.createPBMailItem(mail1));
			}
			
			player_out.write(ResponseCode.MAIL_RETURN_AGEENT_TRANSFER
					, CommonCreator.createPBInt32(200));

		});
	}

	private void actionSendAgentMessge(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		int player_id = player.getPlayerId();

		Common.PBIntString request = message.get();
		int agent_id = request.getKey();
		String content = request.getValue();

		AgentInfoData agent_info_data = AgentInfoProvider.getInst().getAgentInfoByAgentId(agent_id,  player.getPlatform_id());
		if (null == agent_info_data) {
			logger.debug("error agent_id:" + agent_id);
			return;
		}
		ProcLogic.saveAgentMessage(agent_id, player_id, content);

		/// 这个代理商是否自己回复
		String ret_content = AgentAuickReplyProvider.getInst().getAuickReply(agent_id);
		if(0 < ret_content.length()) {
			CenterHandler.handleMainSendMail(player_id, ret_content, "", 10);
		}

		player.write(ResponseCode.MAIL_RETURN_AGEENT_RESPONSE
				, CommonCreator.createPBInt32(200));
	}

	private void actionRequestAgentInfo(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}
		String agentStr = AgentInfoProvider.getInst().getAgentInfoByPlan(player.getPlatform_id(), player.getAgent_plan());
		// 总开关
		if (!DynamicPropertiesPublicProvider.getInst().isOpen(DynamicPublicConst.AGENT_KEY)) {
			player.write(ResponseCode.ACCOUNT_MODIFY_AGENT_INFO, CommonCreator.createPBString(""));
			logger.error("无法显示代理 全局代理开关为关闭状态");
			return;
		}
		// 区域限制
		if (!player.getProvinceData().getAgent()) {
			player.write(ResponseCode.ACCOUNT_MODIFY_AGENT_INFO, CommonCreator.createPBString(""));
			logger.error("无法显示代理  {}区域代理开关为关闭状态", player.getProvince());
			return;
		}
		player.write(ResponseCode.ACCOUNT_MODIFY_AGENT_INFO, CommonCreator.createPBString(agentStr));
	}


	private void actionContackKefu(ChannelHandlerContext ioSession, CocoPacket packet, AbstractHandlers.MessageHolder<MessageLite> message) {
		Player player = PlayerManager.getInstance().getPlayerById(packet.getPlayerId());
		if (player == null) {
			return;
		}

		Common.PBPairString request = message.get();
		boolean bFlag = false;
		if (0 < request.getKey().length() && 0 != Long.valueOf(request.getKey())) {            /// 这是一条投述信息
			if(0 != player.getMem_game_no() && Long.valueOf(request.getKey()) == player.getMem_game_no()) {
				return ;
			}
			/// 这是当天的投述信息
			if (true == PlayerSaver.isComplaint(player)) {
				int conf_total = ChannelInfoProvider.getInst().getChannelConfig(player.getChannelId(), player.getPackageId(), player.isReview()).getComplaint_total();
				if (player.getComplaint_total() > conf_total) {
					logger.error("kefu_complaint player_id:" + player.getPlayerId() + "  player_total:" + player.getComplaint_total() + "  conf_total:" + conf_total);
					return;
				} else {
					player.addComplaint_total(1);
					bFlag = true;
				}
			} else {
				player.setComplaint_total(1);
				player.setComplaint_total_time(MiscUtil.getCurrentSeconds());
				bFlag = true;
			}
			player.setMem_game_no(Long.valueOf(request.getKey()));
		}

//		Common.PBString request = message.get();		//这边不进行处理了
		Map<String, String> data = new HashMap<>();
		data.put("player_id", String.valueOf(player.getPlayerId()));
		data.put("content", request.getValue());
		data.put("contact_time", String.valueOf(MiscUtil.getCurrentSeconds()));
		data.put("phone_num", player.getPhone_num());
		data.put("name", player.getName());
		data.put("game_no", request.getKey());

		if (true == bFlag) {
			CenterActorManager.getHttpActor().put(() -> {
				http.HttpUtil.sendPost(CenterServer.getInst().getBackUrl() + "/service/complaint_info", data, e -> {
				});
				return null;
			});
		} else {
			CenterActorManager.getHttpActor().put(() -> {
				http.HttpUtil.sendPost(CenterServer.getInst().getBackUrl() + "/service/contentKefu", data, e -> {
				});
				return null;
			});
		}

		player.write(ResponseCode.MAIL_CONTACT_KEFU, null);
	}
}
