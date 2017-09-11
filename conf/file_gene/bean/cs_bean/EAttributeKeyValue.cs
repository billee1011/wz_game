using System.Collections.Generic;
using System.Text;

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

	public string toString(){
		StringBuilder builder = new StringBuilder();
		builder.Append("attrId");
		builder.Append(":");
		builder.Append(attrId);
		builder.Append(",");
		builder.Append("value");
		builder.Append(":");
		builder.Append(value);
		return builder.ToString();
	}
}