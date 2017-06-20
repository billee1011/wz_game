package trans_data;

import data.BaseData;

import java.util.Map;

/**
 * Created by think on 2017/6/20.
 */
public class LoginResultMsg extends BaseData {
    private int playerId;

    private Map<Integer, Integer> resourceMap;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Map<Integer, Integer> getResourceMap() {
        return resourceMap;
    }

    public void setResourceMap(Map<Integer, Integer> resourceMap) {
        this.resourceMap = resourceMap;
    }
}
