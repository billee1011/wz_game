package logic.record.detail;

import java.util.ArrayList;
import java.util.List;

import logic.poker.niuniu.zhuang.RoomNiuNiuDeskInfo;

public class GrabNiuDetail {
	/** 游戏类型 就是GameType */
	private int type;
	/** 玩家位置信息列表 此信息 */
	private List<OnePosDetail> records;

	public GrabNiuDetail(int type) {
		this.type = type;
		records = new ArrayList<>();
	}

	public void addPlayerRecord(RoomNiuNiuDeskInfo info, int costTax, long preCoin, int result,
			int winCoin) {
		OnePosDetail detail = new OnePosDetail(info.getPlayerInfo().getPlayerId(), info.getPositionValue(), result,
				costTax, info.getPlayerInfo().getChannel_id(), info.getPlayerInfo().getPackage_id(),
				info.getPlayerInfo().getDevice(), preCoin, info.getPlayerInfo().getCoin(),
				info.getPlayerInfo().getIp());
		records.add(detail);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<OnePosDetail> getRecords() {
		return records;
	}

	public void setRecords(List<OnePosDetail> records) {
		this.records = records;
	}

}
