package logic.majiong;

import java.util.List;

public class HuInfo {
	
	private int resultFan;

	private int huValue;
	
	private int coin;

	private PlayerInfo player;
	
	private PlayerInfo failPlayer;
	
	private XueNiuCalType xueNiuCalType;

	private List<XueniuFanType> typeList;

	public HuInfo(int resultFan, int huValue, int coin, PlayerInfo player, PlayerInfo failPlayer, XueNiuCalType xueNiuCalType, List<XueniuFanType> typeList) {
		this.resultFan = resultFan;
		this.huValue = huValue;
		this.coin = coin;
		this.player = player;
		this.failPlayer = failPlayer;
		this.xueNiuCalType = xueNiuCalType;
		this.typeList = typeList;
	}

	public int getResultFan() {
		return resultFan;
	}

	public void setResultFan(int resultFan) {
		this.resultFan = resultFan;
	}

	public int getHuValue() {
		return huValue;
	}

	public void setHuValue(int huValue) {
		this.huValue = huValue;
	}
	
	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public PlayerInfo getPlayer() {
		return player;
	}

	public void setPlayer(PlayerInfo player) {
		this.player = player;
	}
	
	public PlayerInfo getFailPlayer() {
		return failPlayer;
	}

	public void setFailPlayer(PlayerInfo failPlayer) {
		this.failPlayer = failPlayer;
	}

	public XueNiuCalType getXueNiuCalType() {
		return xueNiuCalType;
	}

	public void setXueNiuCalType(XueNiuCalType xueNiuCalType) {
		this.xueNiuCalType = xueNiuCalType;
	}

	public List<XueniuFanType> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<XueniuFanType> typeList) {
		this.typeList = typeList;
	}
	
}
