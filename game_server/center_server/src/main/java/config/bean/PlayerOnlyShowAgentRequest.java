package config.bean;

import util.MapObject;

import java.util.List;

/**
 * Created by admin on 2017/4/8.
 */
public class PlayerOnlyShowAgentRequest {
    List<MapObject> list;

    public List<MapObject> getList() {
        return list;
    }

    public void setList(List<MapObject> list) {
        this.list = list;
    }
}
