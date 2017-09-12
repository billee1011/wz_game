package bean;

import annotation.EnumField;
public class Equip{
	private int id; 

	private String name; 

	private int attribute; 

	private EItemQuality quality; 

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	public int getAttribute(){
		return attribute;
	}

	public void setAttribute(int attribute){
		this.attribute = attribute;
	}

	public EItemQuality getQuality(){
		return quality;
	}

	public void setQuality(EItemQuality quality){
		this.quality = quality;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("id");
		builder.append(":");
		builder.append(id);
		builder.append(",");
		builder.append("name");
		builder.append(":");
		builder.append(name);
		builder.append(",");
		builder.append("attribute");
		builder.append(":");
		builder.append(attribute);
		builder.append(",");
		builder.append("quality");
		builder.append(":");
		builder.append(quality);
		return builder.toString();
	}
}