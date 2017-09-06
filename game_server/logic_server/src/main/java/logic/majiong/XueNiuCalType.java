package logic.majiong;

/**
 * Created by Administrator on 2016/12/22.
 */
public enum XueNiuCalType {
	HU(1),
	ZIMO(2),
	GUAFENG(3),
	XIAYU(4),
	HUAZHU(5),
	DAJIAO(6),
	TRAN(7),
	DRAWBACK(8),
	YIPAODUOXIANG(9),
	ROOM_CHARGE(10),                    //房费
	ZIMO_DIANPAO(21),           //  新增類型 自摸儅點炮
	;

	private int value;

	XueNiuCalType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

}
