package protobuf.creator;

import chr.Player;
import config.bean.ChannelConfig;
import config.provider.ChannelInfoProvider;
import protobuf.Account;
import service.CenterServer;
import util.MiscUtil;

public class AccountCreator {
	public static Account.PBLoginSuccRes createPBLoginSucc(Player player) {
		Account.PBLoginSuccRes.Builder builder = Account.PBLoginSuccRes.newBuilder();
		builder.setAccountId(player.getAccountId());
		builder.setPlayerId(player.getPlayerId());
		builder.setNickname(player.getName());
		builder.setCoin(player.getCoin()+"");
		builder.setHeadIcon(player.getIcon());
		builder.setBankCoin(player.getBankMoney()+"");
		builder.setGender(player.getGender().getValue());
		builder.setPhoneNum(player.getPhone_num());
		builder.setAlipayAccount(player.getAlipayAccount());
		builder.setAlipayName(player.getAlipayName());
		builder.setRoomId(player.getDeskRoomId());
		builder.setServerTime(MiscUtil.getCurrentSeconds());
//		if(WordBadUtil.hasBadProvince(player.getProvince())){
//			builder.setProvince(WordBadUtil.DEFAULT_PROVINCE);
//			builder.setCity(WordBadUtil.DEFAULT_CITY);
//		}else{
			builder.setProvince(player.getProvince());
			builder.setCity(player.getCity());
//		}
		builder.setComplaintNum(player.getComplaint_total());
		builder.setOnlyShowAgent(player.isOnly_show_agent());

		builder.setVoiceId("");
		builder.setVoiceKey("");
		builder.setShowBankMark(player.getShowBankMark());
		ChannelConfig channel_conf = ChannelInfoProvider.getInst().getChannelConfig(player.getChannelId(), player.getPackageId(), player.isReview());
		if(null != channel_conf) {
			builder.setVoiceId(null == channel_conf.getVoice_id() ? "" : channel_conf.getVoice_id());
			builder.setVoiceKey(null == channel_conf.getVoice_key() ? "" : channel_conf.getVoice_key());
		}
		return builder.build();
	}

	public static Account.PBGeneOrder createPBGeneOrder(String order, int num, String url, String method) {
		Account.PBGeneOrder.Builder builder = Account.PBGeneOrder.newBuilder();
		builder.setOrderId(order);
		builder.setChargeValue(num);
		builder.setOpenMethod(method);
		builder.setPayUrl(url);
		return builder.build();
	}

	public static Account.PBLoginSuccRes createPBLoginAndDynamic(Player player){
		Account.PBLoginSuccRes.Builder builder = Account.PBLoginSuccRes.newBuilder();
		builder.setAccountId(player.getAccountId());
		builder.setPlayerId(player.getPlayerId());
		builder.setNickname(player.getName());
		builder.setCoin(player.getCoin()+"");
		builder.setHeadIcon(player.getIcon());
		builder.setBankCoin(player.getBankMoney()+"");
		builder.setGender(player.getGender().getValue());
		builder.setPhoneNum(player.getPhone_num());
		builder.setAlipayAccount(player.getAlipayAccount());
		builder.setAlipayName(player.getAlipayName());
		builder.setRoomId(player.getDeskRoomId());
		builder.setServerTime(MiscUtil.getCurrentSeconds());
//		if(WordBadUtil.hasBadProvince(player.getProvince())){
//			builder.setProvince(WordBadUtil.DEFAULT_PROVINCE);
//			builder.setCity(WordBadUtil.DEFAULT_CITY);
//		}else{
			builder.setProvince(player.getProvince());
			builder.setCity(player.getCity());
//		}
		builder.setComplaintNum(player.getComplaint_total());
		builder.setOnlyShowAgent(player.isOnly_show_agent());
		builder.setVoiceId("");
		builder.setVoiceKey("");
		builder.setSessionId(player.getToken());
		builder.setShowBankMark(player.getShowBankMark());
		ChannelConfig channel_conf = ChannelInfoProvider.getInst().getChannelConfig(player.getChannelId(), player.getPackageId(), player.isReview());
		if(null != channel_conf) {
			builder.setVoiceId(null == channel_conf.getVoice_id() ? "" : channel_conf.getVoice_id());
			builder.setVoiceKey(null == channel_conf.getVoice_key() ? "" : channel_conf.getVoice_key());
		}
		builder.setDynamicConfig(CenterServer.getInst().createPBDynamicConfig(player.getChannelId(),player.getPackageId(),player));
		return builder.build();
	}
}
