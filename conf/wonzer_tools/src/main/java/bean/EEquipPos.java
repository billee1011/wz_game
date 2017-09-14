package bean;

public enum EEquipPos{
	HEAD(1),
	WEAPON(2),
	BELT(3),
	CLOTHES(4),
	TREASURE_ATTACK(5),
	TREASURE_DEFENCE(6);

	EEquipPos(int value){
		this.value = value;
	}

	private int value;

	public int getValue(){
		return this.value;
	}

	public static EEquipPos getByValue(int value){
		for(EEquipPos type : values()){
			if(type.getValue() == value){
				return type;
			}
		}
		return null;
	}

}