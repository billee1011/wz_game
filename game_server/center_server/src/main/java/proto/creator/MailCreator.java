package proto.creator;

import config.bean.TransferData;
import mail.MailEntity;
import proto.Mail;

import java.util.List;

/**
 * Created by Administrator on 2016/11/29.
 */
public class MailCreator {

	public static Mail.PBAllMailRes createPBAllMailRes(List<MailEntity> mailList) {
		Mail.PBAllMailRes.Builder builder = Mail.PBAllMailRes.newBuilder();
		mailList.forEach(e -> builder.addMail(createPBMailItem(e)));
		return builder.build();
	}

	public static Mail.PBMailItem createPBMailItem(MailEntity mail) {
		Mail.PBMailItem.Builder builder = Mail.PBMailItem.newBuilder();
		builder.setMailId(mail.getMailId());
		builder.setSender(mail.getSender());
		builder.setGain(mail.isGain());
		if (mail.getParams() != null) {
			mail.getParams().forEach(e -> builder.addParams(e));
		}
		builder.setMoney(mail.getMoney());
		builder.setId(mail.getId());
		builder.setRead(mail.isRead());
		builder.setSendTime(mail.getSendTime());
		builder.setTitle(mail.getTitle());
		return builder.build();
	}

	public static Mail.PBTransferList createPBTransferList(List<TransferData> transfer_list, int count_page, int cur_count_page) {
		Mail.PBTransferList.Builder builder = Mail.PBTransferList.newBuilder();
		transfer_list.forEach(e -> builder.addTransferList(createPBTransferData(e)));
		builder.setCountPage(count_page);
		builder.setCurCountPage(cur_count_page);
		return builder.build();
	}

	public static Mail.PBTransferData createPBTransferData(TransferData transfer_data) {
		Mail.PBTransferData.Builder builder = Mail.PBTransferData.newBuilder();
		builder.setId(transfer_data.getId());
		builder.setPlayerIdOut(transfer_data.getPlayer_out_id());
		builder.setPlayerIdIn(transfer_data.getPlayer_in_id());
		builder.setPlayerNameIn(transfer_data.getPlayer_in_name());
		builder.setAmount(transfer_data.getAmount()+"");
		builder.setTime(transfer_data.getTime());
		builder.setType(transfer_data.getType());
		return builder.build();
	}

}
