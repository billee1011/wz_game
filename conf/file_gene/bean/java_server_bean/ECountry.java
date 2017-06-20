package bean;

public enum ECountry{
	MAIN_ROLE(1),
	SHU(2),
	WEI(3),
	WU(4),
	QUN(5);

	ECountry(int value){
		this.value = value;
	}

	private int value;

	public int getValue(){
		return this.value;
	}

	public static ECountry getByValue(int value){
		for(ECountry type : values()){
			if(type.getValue() == value){
				return type;
			}
		}
		return null;
	}

}