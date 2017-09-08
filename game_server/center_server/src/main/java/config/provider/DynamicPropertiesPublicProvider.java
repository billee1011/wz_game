package config.provider;

import config.JsonUtil;
import config.bean.DynamicPropertiesPublicData;
import database.DataQueryResult;
import define.constant.DynamicPublicConst;
import util.MapObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/4/17.
 */
public class DynamicPropertiesPublicProvider extends BaseProvider {

    private static DynamicPropertiesPublicProvider inst = new DynamicPropertiesPublicProvider();

    private DynamicPropertiesPublicProvider() {

    }

    public static DynamicPropertiesPublicProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<Integer, DynamicPropertiesPublicData> dynamic_properties_pulibc = null;

    @Override
    protected void initString() {

    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	Map<Integer, DynamicPropertiesPublicData> dynamic_properties_pulibc = new HashMap<>();
        List<MapObject> data_list = DataQueryResult.load("SELECT * FROM conf_dynamic_properties_public");
        for (MapObject data_info : data_list) {
            DynamicPropertiesPublicData dppd = new DynamicPropertiesPublicData();
            int id = data_info.getInt("id");
            dppd.setId(id);
            dppd.setType(data_info.getInt("type"));
            dppd.setStatus(data_info.getInt("status"));
            dppd.setKey(data_info.getString("key"));
            dppd.setValue(data_info.getString("value"));

            dynamic_properties_pulibc.put(id, dppd);
        }
        this.dynamic_properties_pulibc = dynamic_properties_pulibc;
    }

    public boolean isOpen(String key) {
        for(Map.Entry<Integer, DynamicPropertiesPublicData> entry : dynamic_properties_pulibc.entrySet()) {
            if(true == entry.getValue().getKey().equals(key)) {
                return entry.getValue().getStatus() == 1;
            }
        }
        return false;
    }
    
    private String getValue(String key) {
        for(Map.Entry<Integer, DynamicPropertiesPublicData> entry : dynamic_properties_pulibc.entrySet()) {
            if(true == entry.getValue().getKey().equals(key)) {
                return entry.getValue().getValue();
            }
        }
        return null;
    }
    
    /**
     * 获取支付开关
     * @return
     */
	public String getPayTypeStatus() {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(1, isOpen(DynamicPublicConst.ALIPAY_KEY) ? 1 : 0);
		map.put(2, isOpen(DynamicPublicConst.WEIXIIN_KEY) ? 1 : 0);
		map.put(3, isOpen(DynamicPublicConst.UNIONPAY_KEY) ? 1 : 0);
		return JsonUtil.getGson().toJson(map, Map.class);
	}
	
	/**
	 * 获取共享区域名字
	 * @return
	 */
	public String getProvinceOtherName(){
		return getValue(DynamicPublicConst.PROVINCE_OTHER_NAME_KEY);
	}
	
	/**
	 * 共享区域是否进审核
	 * @return
	 */
	public boolean isProvinceReview(){
		return isOpen(DynamicPublicConst.PROVINCE_REVIEW_KEY);
	}
	
}
