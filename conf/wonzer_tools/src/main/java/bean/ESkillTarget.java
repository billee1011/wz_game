package bean;

public enum ESkillTarget{
	FORWARD_SINGLE(1),
	BACK_SINGLE(2),
	FORWARD_ROW(3),
	BACK_ROW(4),
	COL(5),
	RANDOM_COL(6),
	HP_MAX_COL(7),
	HP_MIN_COL(8),
	RANDOM_TWO_COL(9),
	ALL(10),
	RANDOM_ONE(11),
	RANDOM_TWO(12),
	RANDOM_THREE(13),
	RANDOM_FOUR(14),
	RANDOM_FIVE(15),
	HP_MIN_SINGLE(16),
	HP_MAX_SINGLE(17),
	ATTACK_MAX_SINGLE(18),
	ATTACK_MIN_SINGLE(19);

	ESkillTarget(int value){
		this.value = value;
	}

	private int value;

	public int getValue(){
		return this.value;
	}

	public static ESkillTarget getByValue(int value){
		for(ESkillTarget type : values()){
			if(type.getValue() == value){
				return type;
			}
		}
		return null;
	}

}