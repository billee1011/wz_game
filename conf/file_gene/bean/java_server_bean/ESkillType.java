package bean;

public enum ESkillType{
	NORMAL(1),
	ANGRY(2),
	COMPOSITE(3),
	SUPER_COMPOSITE(4);

	ESkillType(int value){
		this.value = value;
	}

	private int value;

	public int getValue(){
		return this.value;
	}

	public static ESkillType getByValue(int value){
		for(ESkillType type : values()){
			if(type.getValue() == value){
				return type;
			}
		}
		return null;
	}

}