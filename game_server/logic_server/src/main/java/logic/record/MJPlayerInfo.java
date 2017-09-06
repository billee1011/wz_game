package logic.record;

import logic.majiong.PlayerInfo;

/**
 * Created by Administrator on 2017/1/12.
 */
public class MJPlayerInfo {
	private int player_id;
	private int position;
	private String name;
	private String icon;
	private long coin;
	private int gender;
	private String province;
	private String city;

	public MJPlayerInfo(int position, PlayerInfo player) {
		this.position = position;
		this.player_id = player.getPlayerId();
		this.name = player.getName();
		this.icon = player.getIcon();
		this.coin = player.getCoin();
		this.gender = player.getGender().getValue();
		this.province = player.getProvince();
		this.city = player.getCity();
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

	public int getPlayer_id() {
		return player_id;
	}

	public void setPlayer_id(int player_id) {
		this.player_id = player_id;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
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

	public long getCoin() {
		return coin;
	}

	public void setCoin(long coin) {
		this.coin = coin;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}
}
