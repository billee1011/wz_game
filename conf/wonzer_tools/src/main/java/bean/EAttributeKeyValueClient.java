package bean;

public class EAttributeKeyValueClient{
	private int attrId; 

	private int value; 

	public int getAttrId(){
		return attrId;
	}

	public void setAttrId(int attrId){
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