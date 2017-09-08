package bean;

public enum EMoney{
	DIAMOND(101),
	SILVER(102),
	REPUTATION(103),
	ZHANGONG(104),
	GOLD(105),
	JIANGHUN(106),
	SHENHUN(107),
	WEIMING(108),
	SHOUHUN(109),
	HUNJING(110),
	QIYUDIAN(111),
	LINGYU(112),
	MINGJIANGLING(113),
	MINGJIANGLING2(114);

	EMoney(int value){
		this.value = value;
	}

	private int value;

	public int getValue(){
		return this.value;
	}

	public static EMoney getByValue(int value){
		for(EMoney type : values()){
			if(type.getValue() == value){
				return type;
			}
		}
		return null;
	}

}