package config.provider;

import config.bean.Province;
import database.DataQueryResult;
import service.CenterServer;
import util.ASObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by admin on 2017/4/16.
 */
public class ProvinceProvider extends BaseProvider  {
	private static Logger logger = LoggerFactory.getLogger(ProvinceProvider.class);
    private static ProvinceProvider inst = new ProvinceProvider();
    
    private ProvinceProvider() {

    }

    public static ProvinceProvider getInst() {
        return inst;
    }

    static {
        BaseProvider.providerList.add(inst);
    }

    private Map<String, Province> provinceMap = null;

    @Override
    protected void initString() {

    }

    @Override
    public void doLoad() {
        getInfo();
    }

    private void getInfo() {
    	Map<String, Province> provinceMap = new HashMap<>();
        List<ASObject> data_list = DataQueryResult.load("select * from conf_province");
        for (ASObject data_info : data_list) {
        	Province province = new Province();
        	province.setId(data_info.getInt("id"));
        	province.setProvinceName(data_info.getString("province"));
        	province.setAnnoUrl(data_info.getString("anno_url"));
        	province.setAnnoId(data_info.getInt("anno_id"));
        	province.setPayUrl(data_info.getString("pay_url"));
//        	province.setPayKey(data_info.getString("pay_key"));
        	province.setExchangeUrl(data_info.getString("exchange_url"));
//        	province.setExchangeKey(data_info.getString("exchange_key"));
        	province.setAccountUp(data_info.getInt("account_up"));
        	province.setAlipay(data_info.getInt("alipay"));
        	province.setWeixin(data_info.getInt("weixin"));
        	province.setUnionpay(data_info.getInt("unionpay"));
        	province.setAgent(data_info.getInt("agent"));
        	province.setExchange(data_info.getInt("exchange"));
        	province.setMaintenance(data_info.getInt("maintenance"));
        	
        	provinceMap.put(province.getProvinceName(), province);
        }
        
        this.provinceMap  = provinceMap;
    }

    public Province getData(String name) {
		Province province = provinceMap.get(name);
		if (province == null) {
			province = provinceMap.get(DynamicPropertiesPublicProvider.getInst().getProvinceOtherName());
		}
		if (province == null){
			logger.error("找不到 {} 地区配置 且并没用配置  共享地址!", name);
			return provinceMap.values().stream().findFirst().get();	
		}
		return province;
    }
    
}
