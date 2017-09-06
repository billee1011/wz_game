package logic.majiong.xueniu;

import logic.majiong.XueniuFanType;
import protobuf.Common;
import protobuf.Xueniu;

import java.util.List;

/**
 * Created by Administrator on 2016/12/22.
 */
public class XNOneCalRecord {

	private List<XueniuFanType> typeList;

	private Xueniu.PBCalculate cal;

	public XNOneCalRecord(List<XueniuFanType> typeList, Xueniu.PBCalculate cal) {
		this.typeList = typeList;
		this.cal = cal;
	}

	public int getTimes() {
		return this.cal.getTimes();
	}

	public int getPositionGain() {
		for (Common.PBPair pair : cal.getResultList()) {
			if (pair.getKey() == cal.getPosition()) {
				return pair.getValue();
			}
		}
		return 0;
	}

	public int getPosition() {
		return cal.getPosition();
	}

	public int getPositionGainLose(int position) {
		for (Common.PBPair pair : cal.getResultList()) {
			if (pair.getKey() == position) {
				return pair.getValue();
			}
		}
		return 0;
	}

	public boolean containPosition(int pos) {
		for (Common.PBPair pair : cal.getResultList()) {
			if (pair.getKey() == pos) {
				return true;
			}
		}
		return false;
	}


	public List<XueniuFanType> getTypeList() {
		return typeList;
	}

	public void setTypeList(List<XueniuFanType> typeList) {
		this.typeList = typeList;
	}

	public Xueniu.PBCalculate getCal() {
		return cal;
	}

	public void setCal(Xueniu.PBCalculate cal) {
		this.cal = cal;
	}
}
