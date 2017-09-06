package common;

import com.google.protobuf.MessageLite;
import data.BankAction;
import data.MoneyAction;
import data.logdata.*;
import util.MiscUtil;

/**
 * Created by think on 2017/3/21.
 */
public class LogHelper {
	public static MessageLite logAccount(int userId, int time, int channel
			, int action, int packageId, String ip, String province, String city, String device) {
		ILogInfo logInfo = new LogAccount(userId, action, ip, device, channel, packageId, time, province, city);
		return logInfo.write();
	}

	public static MessageLite logOnline(int playerId, int channel, int packageId
			, int action, String ip, String province, String city, String device, String machineId) {
		int time = MiscUtil.getCurrentSeconds();
		ILogInfo logInfo = new LogOnline(playerId, action, time, ip, province, city, packageId, channel, device, 0, machineId);
		return logInfo.write();
	}

	public static MessageLite logBankSave(int playerId, int amount, int bagCoin, int bankCoin, long pre_bank_coin, String ip, int channel_id, String package_id, String device) {
		int time = MiscUtil.getCurrentSeconds();
		ILogInfo logInfo = new LogBank(playerId, time, BankAction.SAVE.getValue(), amount, bagCoin, bankCoin, pre_bank_coin, ip, channel_id, package_id, device);
		return logInfo.write();
	}

	public static MessageLite logBankWithdraw(int playerId, int amount, int bagCoin, int bankCoin, long pre_bank_coin,  String ip, int channel_id, String package_id, String device) {
		int time = MiscUtil.getCurrentSeconds();
		ILogInfo logInfo = new LogBank(playerId, time, BankAction.WITHDRAW.getValue(), amount, bagCoin, bankCoin, pre_bank_coin, ip, channel_id, package_id, device);
		return logInfo.write();
	}

	public static MessageLite logBankSave_ex(int playerId, int flag, int amount, long bagCoin, long bankCoin, long pre_bank_coin, String ip, int channel_id, String package_id, String device) {
		int time = MiscUtil.getCurrentSeconds();
		ILogInfo logInfo = new LogBank(playerId, time, flag, amount, bagCoin, bankCoin, pre_bank_coin, ip, channel_id, package_id, device);
		return logInfo.write();
	}

	public static MessageLite logMoney(int playerId, int action, int subAction, int gameId, long coin, long pre_coin, long last_coin, String ip, int channel_id, String package_id, String device, long game_no) {
		int time = MiscUtil.getCurrentSeconds();
		ILogInfo logInfo = new LogMoney(playerId, Math.abs(coin), action, subAction, time, gameId, pre_coin, last_coin, ip, channel_id, package_id, device, game_no);
		return logInfo.write();
	}

	public static MessageLite logGainMoney(int playerId, int subAction, int gameId, int coin, long pre_coin, long last_coin, String ip, int channel_id, String package_id, String device, long game_no) {
		return logMoney(playerId, MoneyAction.GAIN.getValue(), subAction, gameId, coin, pre_coin, last_coin, ip, channel_id, package_id, device, game_no);
	}

	public static MessageLite logLoseMoney(int playerId, int subAction, int gameId, int coin, long pre_coin, long last_coin, String ip, int channel_id, String package_id, String device, long game_no) {
		return logMoney(playerId, MoneyAction.LOSE.getValue(), subAction, gameId, coin, pre_coin, last_coin, ip, channel_id, package_id, device, game_no);
	}
	
	public static MessageLite logRoomGainMoney(int roomId,int playerId, int subAction, int gameId, int coin, long pre_coin, long last_coin, String ip, int channel_id, String package_id, String device, long game_no) {
		return logMoney(playerId, MoneyAction.GAIN.getValue(), subAction, gameId, coin, pre_coin, last_coin, ip, channel_id, package_id, device, game_no);
	}

	public static MessageLite logRoomLoseMoney(int roomId,int playerId, int subAction, int gameId, int coin, long pre_coin, long last_coin, String ip, int channel_id, String package_id, String device, long game_no) {
		return logMoney(playerId, MoneyAction.LOSE.getValue(), subAction, gameId, coin, pre_coin, last_coin, ip, channel_id, package_id, device, game_no);
	}

}
