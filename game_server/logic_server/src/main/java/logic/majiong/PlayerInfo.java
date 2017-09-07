package logic.majiong;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.MessageLite;

import common.LogHelper;
import data.MoneySubAction;
import logic.Desk;
import logic.DeskMgr;
import logic.majiong.define.Gender;
import packet.CocoPacket;
import proto.creator.CommonCreator;
import protocol.c2s.RequestCode;
import protocol.s2c.ResponseCode;
import service.LogicApp;
import util.LogUtil;

/**
 * Created by Administrator on 2017/2/7.
 */
public class PlayerInfo {
	private static Logger logger = LoggerFactory.getLogger(PlayerInfo.class);
	private int playerId;
	private String name;
	private String icon;
	private int coin;
	private Gender gender;
	private int position;
	private String province;
	private String city;
	private int old_coin;
	private int channel_id;
	private int package_id;
	private String device;
	private String ip;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getChannel_id() {
		return channel_id;
	}

	public void setChannel_id(int channel_id) {
		this.channel_id = channel_id;
	}

	public int getPackage_id() {
		return package_id;
	}

	public void setPackage_id(int package_id) {
		this.package_id = package_id;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public int getOld_coin() {
		return old_coin;
	}

	public void setOld_coin(int old_coin) {
		this.old_coin = old_coin;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Gender getGender() {
		return gender;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void updateCoin(long value, boolean add) {
		updateCoin(value, add, null, 0, 0);            //适配之前的
	}

	public void updateCoin(long value, boolean add, int type) {
		if (add) {
			this.coin += value;
		} else {
			this.coin -= value;
		}
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk) {
			if (desk.isPlayerLeave(this)) {
				LogicApp.getInst().getClient().sendRequest(new CocoPacket(ResponseCode.ACCOUNT_UPDATE_COIN.getValue(), CommonCreator.createPBInt32(coin).toByteArray(), getPlayerId()));
			}
		}
		logger.info("{} 玩家 {} {} coin {},剩余{} ", 0, this.playerId, add ? "win" : "lost", value, this.coin);

		LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_UPDATE_PLAYER_COIN
				, CommonCreator.createPBStringList(new ArrayList<String>(Arrays.asList("" + value, add ? "1" : "0", "" + type,desk.getConfId()+"",desk.isPersonal()?"1":"0"))), getPlayerId()));
	}

	public void updateCoin(long value, boolean add, MoneySubAction action, int gameType, long game_no) {
		int preCoin = this.coin;
		if (add) {
			this.coin += value;
		} else {
			this.coin -= value;
			if(this.coin < 0){
				this.coin = 0;
				value = preCoin;
				logger.error("游戏扣钱异常{} -{} 之前{},操作{},no{}",playerId,value,preCoin,action,game_no);
			}
		}
		if (action != null) //  现在只有牛牛和斗地主 在这里记LOG  其他自己记录  后续是不是要改成这边统一记录
			if (add){
				write(RequestCode.LOG_MONEY.getValue()
						, LogHelper.logGainMoney(getPlayerId(), action.getValue(), gameType, (int) value, preCoin, getCoin(), getIp(), getChannel_id(), String.valueOf(getPackage_id()), getDevice(), game_no));
			} else {
				write(RequestCode.LOG_MONEY.getValue()
						, LogHelper.logLoseMoney(getPlayerId(), action.getValue(), gameType, (int) value, preCoin, getCoin(), getIp(), getChannel_id(), String.valueOf(getPackage_id()), getDevice(), game_no));
			}
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(getPlayerId());
		if (desk == null) {
			return;
		}
		if (desk instanceof MJDesk) {
			if (desk.isPlayerLeave(this)) {
				LogicApp.getInst().getClient().sendRequest(new CocoPacket(ResponseCode.ACCOUNT_UPDATE_COIN.getValue(), CommonCreator.createPBInt32(coin).toByteArray(), getPlayerId()));
			}
		}
		logger.info("{} 玩家 {} {} coin {},剩余{} ", game_no, this.playerId, add ? "win" : "lost", value, this.coin);

		LogicApp.getInst().getClient().sendRequest(new CocoPacket(RequestCode.CENTER_UPDATE_PLAYER_COIN
				, CommonCreator.createPBStringList(new ArrayList<String>(Arrays.asList("" + value, add ? "1" : "0", "1",desk.getConfId()+"",desk.isPersonal()?"1":"0"))), getPlayerId()));
	}


	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public void write(ResponseCode code, MessageLite message) {
		Desk desk = DeskMgr.getInst().getDeskByPlayerId(getPlayerId());
		if (desk == null) {
			logger.debug(" the player {} is null", getPlayerId());
			return;
		}
		if (desk.isPlayerLeave(this)) {
			logger.debug(" the player {} leave the desk ", getPlayerId());
			return;
		}
		if (message != null) {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getPlayerId(), code, message.toByteArray().length, message);
		} else {
			LogUtil.msgLogger.info("player id is {} write message {} and length is {} and the content is {}", getPlayerId(), code);
		}
		LogicApp.getInst().getClient().sendRequest(new CocoPacket(code.getValue(), message == null ? null : message.toByteArray(), getPlayerId()));
	}

	public void write(int code, MessageLite message) {
		LogicApp.getInst().getClient().sendRequest(new CocoPacket(code, message == null ? null : message.toByteArray(), getPlayerId()));
	}

}
