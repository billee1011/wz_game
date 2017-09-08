package config.bean;

public class EAttributeKeyValue{
	private EBattleAttribute attrId; 

	private int value; 

	public EBattleAttribute getAttrId(){
		return attrId;
	}

	public void setAttrId(EBattleAttribute attrId){
		this.attrId = attrId;
	}

	public int getValue(){
		return value;
	}

	public void setValue(int value){
		this.value = value;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("attrId");
		builder.append(":");
		builder.append(attrId);
		builder.append(",");
		builder.append("value");
		builder.append(":");
		builder.append(value);
		return builder.toString();
	}
}