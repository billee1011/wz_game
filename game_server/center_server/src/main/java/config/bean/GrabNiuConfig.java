package config.bean;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import util.MapObject;

public class GrabNiuConfig {
    private List<Integer> grabZhuang = new ArrayList<>();
    private List<Integer> addBet = new ArrayList<>();

    public GrabNiuConfig(MapObject obj) {
        JSONArray grabZhuangJson = JSONArray.fromObject(obj.getString("grab_zhuang"));
        JSONArray addBetJson = JSONArray.fromObject(obj.getString("add_bet"));

        for (int i = 0; i < grabZhuangJson.size(); i++) {
        	grabZhuang.add(grabZhuangJson.getInt(i));
		}

        for (int i = 0; i < addBetJson.size(); i++) {
        	addBet.add(addBetJson.getInt(i));
		}
    }

    public List<Integer> getGrabZhuang() {
        return grabZhuang;
    }

    public void setGrabZhuang(List<Integer> grabZhuang) {
        this.grabZhuang = grabZhuang;
    }

    public List<Integer> getAddBet() {
        return addBet;
    }

    public void setAddBet(List<Integer> addBet) {
        this.addBet = addBet;
    }
}
