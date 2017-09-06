package logic.majiong;

/**
 * Created by Administrator on 2016/12/20.
 */
public enum XueniuFanType {
	PINGHU(1, 1),                    //平胡
	DUIDUI(2, 2),                    //对对胡
    QINGYISE(3, 4),                //清一色
    DAIYAOJIU(4, 4, true),                //带19
	QIDUI(5, 4),                    //七对
	JINGOU(6, 4),                    //金钩钩
	QINGDUI(7, 8),                    //清一色对对胡
	LONGQIDUI(8, 16),                //龙七对
	QINGQIDUI(9, 16),                //清一色七对
	QING19(10, 16),                //清一色带19
	JIANGJINGOU(11, 16),            //清一色金钩钩
    TIANHU(12, 32, true),                //天胡
    DIHU(13, 32, true),                  //地胡
	QINGLONGQIDUI(14, 32),        //清一色龙七对
	SHIBALUOHAN(15, 64),            //十八罗汉
	QINGSHIBALUOHAN(16, 256),        //清一色十八罗汉
	GANGSHANGHUA(17, 1),            //杠上花
	GANGSHANGPAO(18, 1),            //杠上炮
	QIANGGANG(19, 1),                //抢杠胡
	GEN(20, 1),
	//额外番形
//	DAI(21, 3, true),					//带19
	DUAN_19(22, 2, true),                //断19
	JIANG_DUI(23, 8, true),            //将对对胡
	MENQIANQING(24, 2, true),            //门前清

	JIANGJINGGOU2(25,16,false),			// 將金鉤
	END(26, 0),;                    //有根


	private int id;

	private int fan;

	private boolean extra;

	public boolean isExtra() {
		return extra;
	}

	public int getId() {
		return this.id;
	}

	public int getFan() {
		return this.fan;
	}

	XueniuFanType(int id, int fan, boolean extra) {
		this.id = id;
		this.fan = fan;
		this.extra = extra;
	}

	XueniuFanType(int id, int fan) {
		this(id, fan, false);
	}

	public static XueniuFanType getByValue(int id) {
		for (XueniuFanType type : values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		return null;
	}
}
