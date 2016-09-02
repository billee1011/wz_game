package client;

import database.DbObject;

/**
 * Created by WZ on 2016/9/2 0002.
 */
public class LoginCharacter {


	public LoginCharacter(DbObject data) {

	}

	public LoginCharacter(){

	}

	private int userId;

	private int playerId;

	private String name;

	private int gender;

	private int level;

	private int exp;

	private int job;

	public void saveCharacter() {
		DbObject object = new DbObject();
		object.put("user_id", userId);
		object.put("name", name);
		object.put("gender", 1);
		object.put("exp", 1111);
		object.put("job", 1);
		object.put("level", 1);

	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
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

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public static LoginCharacter getDefault(int userId) {
		LoginCharacter character = new LoginCharacter();
		character.setUserId(userId);
		character.setName(String.valueOf(userId));
		return character;
	}
}
