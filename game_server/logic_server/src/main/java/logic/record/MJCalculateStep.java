package logic.record;

/**
 * Created by Administrator on 2017/1/12.
 */
public class MJCalculateStep extends MjStep {
	private MJCalculate calculate;

	public MJCalculateStep(int type, MJCalculate calculate) {
		this.type = type;
		this.calculate = calculate;
	}

	public MJCalculate getCalculate() {
		return calculate;
	}

	public void setCalculate(MJCalculate calculate) {
		this.calculate = calculate;
	}
}
