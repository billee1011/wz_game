package config;

import config.provider.BaseProvider;
import database.DataQueryResult;
import util.ASObject;

import java.util.List;

/**
 * Created by Administrator on 2017/3/5.
 */
public class DynamicInfoProvider extends BaseProvider {
	private int exchange1Min;
	private int exchange1Max;
	private int exchange1Need;
	private int exchange2Min;
	private int exchange2Max;
	private int exchange2Need;
	private int exchange_lowExch;

	private static DynamicInfoProvider instance = new DynamicInfoProvider();

	static {
		BaseProvider.providerList.add(instance);
	}

	private DynamicInfoProvider() {
	}

	public static DynamicInfoProvider getInst() {
		return instance;
	}

	public int getExchange1Min() {
		return exchange1Min;
	}

	public void setExchange1Min(int exchange1Min) {
		this.exchange1Min = exchange1Min;
	}

	public int getExchange1Max() {
		return exchange1Max;
	}

	public void setExchange1Max(int exchange1Max) {
		this.exchange1Max = exchange1Max;
	}

	public int getExchange1Need() {
		return exchange1Need;
	}

	public void setExchange1Need(int exchange1Need) {
		this.exchange1Need = exchange1Need;
	}

	public int getExchange2Min() {
		return exchange2Min;
	}

	public void setExchange2Min(int exchange2Min) {
		this.exchange2Min = exchange2Min;
	}

	public int getExchange2Max() {
		return exchange2Max;
	}

	public void setExchange2Max(int exchange2Max) {
		this.exchange2Max = exchange2Max;
	}

	public int getExchange2Need() {
		return exchange2Need;
	}

	public void setExchange2Need(int exchange2Need) {
		this.exchange2Need = exchange2Need;
	}
	
	public int getExchange_lowExch() {
		return exchange_lowExch;
	}

	public void setExchange_lowExch(int exchange_lowExch) {
		this.exchange_lowExch = exchange_lowExch;
	}

	@Override
	protected void initString() {
		
	}

	@Override
	public void doLoad() {
		List<ASObject> configList = DataQueryResult.load("conf_dynamic_properties", null);
		if (configList.size() != 1) {
			return;
		}
		ASObject data = configList.get(0);
		int exchange_lowExch = data.getInt("exchange_lowExch");
		int exchange1Min = data.getInt("exchange_minExch1");
		int exchange1Max = data.getInt("exchange_maxExch1");
		int exchange1Need = data.getInt("exchange_charge1");
		int exchange2Min = data.getInt("exchange_minExch2");
		int exchange2Max = data.getInt("exchange_maxExch2");
		int exchange2Need = data.getInt("exchange_charge2");
		
		this.exchange_lowExch = exchange_lowExch;
		this.exchange1Min = exchange1Min;
		this.exchange1Max = exchange1Max;
		this.exchange1Need = exchange1Need;
		this.exchange2Min = exchange2Min;
		this.exchange2Max = exchange2Max;
		this.exchange2Need = exchange2Need;
	}

}
