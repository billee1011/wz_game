package bean;

public enum EItemQuality{
	WHITE(1),
	GREEN(2),
	BLUE(3),
	PURPLE(4),
	ORANGE(5),
	RED(6),
	GOLD(7);

	EItemQuality(int value){
		this.value = value;
	}

	private int value;

	public int getValue(){
		return this.value;
	}

	public static EItemQuality getByValue(int value){
		for(EItemQuality type : values()){
			if(type.getValue() == value){
				return type;
			}
		}
		return null;
	}

}