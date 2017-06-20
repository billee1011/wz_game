package bean;

public enum EJob{
	SOLDIER(1),
	MASTER(2);

	EJob(int value){
		this.value = value;
	}

	private int value;

	public int getValue(){
		return this.value;
	}

	public static EJob getByValue(int value){
		for(EJob type : values()){
			if(type.getValue() == value){
				return type;
			}
		}
		return null;
	}

}