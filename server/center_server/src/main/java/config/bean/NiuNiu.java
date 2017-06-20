package config.bean;

/**
 * Created by Administrator on 2016/12/17.
 */
public class NiuNiu {
	private int roomId;
	private int enterLimit;
	private int bannerLimit;
	private int systemCoin;
	private String chips;
	private String roomTip;

	public String getChips() {
		return chips;
	}

	public void setChips(String chips) {
		this.chips = chips;
	}

	public String getRoomTip() {
		return roomTip;
	}

	public void setRoomTip(String roomTip) {
		this.roomTip = roomTip;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getEnterLimit() {
		return enterLimit;
	}

	public void setEnterLimit(int enterLimit) {
		this.enterLimit = enterLimit;
	}

	public int getBannerLimit() {
		return bannerLimit;
	}

	public void setBannerLimit(int bannerLimit) {
		this.bannerLimit = bannerLimit;
	}

	public int getSystemCoin() {
		return systemCoin;
	}

	public void setSystemCoin(int systemCoin) {
		this.systemCoin = systemCoin;
	}
	
}
