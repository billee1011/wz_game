package mail;

import database.DBUtil;
import util.ASObject;
import util.MiscUtil;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2016/11/29.
 */
public class MailEntity implements Serializable {

	public static int pay = 1;					/// 充值
	public static int exchange = 2;			/// 兑换提示
	public static int rank_reward = 3;			/// 排行奖励
	public static int ke_fu = 4;				/// 客服提示
	public static int system = 5;				/// 系统提示
	public static int transfer_out = 6;		/// 转账消息(转出)
	public static int transfer_in = 7;			/// 转账消息(转入)
	public static int upgrade = 8;				/// 升级账号金币

	private static AtomicInteger count = new AtomicInteger(1);

	private int playerId;

	private int id;

	private int mailId;

	private String sender;

	private int contentId;

	private int money;

	private List<String> params;

	private int sendTime;

	private boolean gain;

	private boolean read;

	private int agent_id;

	private String title;

	private MailEntity() {

	}

	public static MailEntity createMail(ASObject data) {
		MailEntity entity = new MailEntity();
		entity.playerId = data.getInt("player_id");
		entity.id = data.getInt("id");
		entity.sender = data.getString("sender");
		entity.sendTime = data.getInt("send_time");
		entity.mailId = data.getInt("mail_id");
		entity.money = data.getInt("money");
		entity.title = data.getString("title");
		for (String param : data.getString("param").split(",")) {
			entity.addParam(param);
		}
		entity.gain = data.getInt("gain") == 1 ? true : false;
		entity.read = data.getInt("readed") == 1 ? true : false;
		entity.agent_id = data.getInt("agent_id");
		entity.title = data.getString("title");
		return entity;
	}


	public static void insertMailIntoDataBase(MailEntity mail) {
		Map<String, Object> mailData = new HashMap<>();
		mailData.put("player_id", mail.getPlayerId());
		mailData.put("id", mail.getId());
		mailData.put("sender", mail.getSender());
		mailData.put("send_time", mail.getSendTime());
		mailData.put("mail_id", mail.getMailId());
		mailData.put("param", mail.getParamString());
		mailData.put("money", mail.getMoney());
		mailData.put("gain", mail.isGain() ? 1 : 0);
		mailData.put("readed", mail.isRead() ? 1 : 0);
		mailData.put("agent_id", mail.getAgent_id());
		mailData.put("title", mail.getTitle());
		try {
			DBUtil.executeInsert("mail", mailData);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}

	//factory method to create mail
	public static MailEntity createMail(int playerId, int mailId, int contentId, int money, String... params) {
		MailEntity entity = new MailEntity();
		entity.setMoney(money);
		entity.setPlayerId(playerId);
		entity.setSendTime(MiscUtil.getCurrentSeconds());
		entity.setMailId(contentId);
		for (int i = 0; i < params.length; i++) {
			entity.addParam(params[i]);
		}
		entity.setId(mailId);
		entity.setSender("system");
		entity.setGain(false);
		entity.setTitle("");
		return entity;
	}

	public static MailEntity createMail_ex(int playerId, int mailId, int contentId, int money, int agent_id, String title, String... params) {
		MailEntity entity = new MailEntity();
		entity.setMoney(money);
		entity.setPlayerId(playerId);
		entity.setSendTime(MiscUtil.getCurrentSeconds());
		entity.setMailId(contentId);
		for (int i = 0; i < params.length; i++) {
			entity.addParam(params[i]);
		}
		entity.setId(mailId);
		entity.setSender("system");
		entity.setGain(false);
		entity.setAgent_id(agent_id);
		entity.setTitle(title);
		return entity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getAgent_id() {
		return agent_id;
	}

	public void setAgent_id(int agent_id) {
		this.agent_id = agent_id;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isGain() {
		return gain;
	}

	public void setGain(boolean gain) {
		this.gain = gain;
	}

	public void addParam(String param) {
		if (params == null) {
			params = new ArrayList<>();
		}
		params.add(param);
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public void setParams(List<String> params) {
		this.params = params;
	}

	public int getMailId() {
		return mailId;
	}

	public void setMailId(int mailId) {
		this.mailId = mailId;
	}

	public String getSender() {
		return sender;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getSendTime() {
		return sendTime;
	}

	public void setSendTime(int sendTime) {
		this.sendTime = sendTime;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public int getContentId() {
		return contentId;
	}

	public void setContentId(int contentId) {
		this.contentId = contentId;
	}

	public List<String> getParams() {
		return params;
	}

	public int getId() {
		return id;
	}

	public String getParamString() {
		StringBuilder builder = new StringBuilder();
		if (params != null) {
			for (int i = 0, length = params.size(); i < length; i++) {
				builder.append(params.get(i));
				if (i != length - 1) {
					builder.append(",");
				}
			}
		}
		return builder.toString();
	}

	public void setId(int id) {
		this.id = id;
	}
}
