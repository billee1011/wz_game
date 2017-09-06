package config;

public class CoupleRoomCfg {
	private int roomId;

	private int baseScore;

	private int minReq;

	private int maxReq;

	private boolean guoDouble;

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getBaseScore() {
		return baseScore;
	}

	public void setBaseScore(int baseScore) {
		this.baseScore = baseScore;
	}

	public int getMinReq() {
		return minReq;
	}

	public void setMinReq(int minReq) {
		this.minReq = minReq;
	}

	public int getMaxReq() {
		return maxReq;
	}

	public void setMaxReq(int maxReq) {
		this.maxReq = maxReq;
	}

	public boolean isGuoDouble() {
		return guoDouble;
	}

	public void setGuoDouble(boolean guoDouble) {
		this.guoDouble = guoDouble;
	}
}
