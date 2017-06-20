package trans_data;

import data.BaseData;

/**
 * Created by think on 2017/6/16.
 */
public class RoleData extends BaseData {
    private int playerId;
    private int gender;
    private String hello;
    private boolean testBoolean;

    public boolean isTestBoolean() {
        return testBoolean;
    }

    public void setTestBoolean(boolean testBoolean) {
        this.testBoolean = testBoolean;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getHello() {
        return hello;
    }

    public void setHello(String hello) {
        this.hello = hello;
    }
}
