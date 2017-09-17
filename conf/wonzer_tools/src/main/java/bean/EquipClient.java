package bean;

import java.util.List;
import annotation.ListDesc;
public class EquipClient{
	private int id; 

	private String name; 

	@ListDesc("EAttributeKeyValue")
	private List<EAttributeKeyValue> attribute; 

	private int quality; 

	@ListDesc("EAttributeKeyValue")
	private List<EAttributeKeyValue> level_add_attribute; 

	private int equip_pos; 

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

	public List<EAttributeKeyValue> getAttribute(){
		return attribute;
	}

	public void setAttribute(List<EAttributeKeyValue> attribute){
		this.attribute = attribute;
	}

	public int getQuality(){
		return quality;
	}

	public void setQuality(int quality){
		this.quality = quality;
	}

	public List<EAttributeKeyValue> getLevel_add_attribute(){
		return level_add_attribute;
	}

	public void setLevel_add_attribute(List<EAttributeKeyValue> level_add_attribute){
		this.level_add_attribute = level_add_attribute;
	}

	public int getEquip_pos(){
		return equip_pos;
	}

	public void setEquip_pos(int equip_pos){
		this.equip_pos = equip_pos;
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
		builder.append(",");
		builder.append("level_add_attribute");
		builder.append(":");
		builder.append(level_add_attribute);
		builder.append(",");
		builder.append("equip_pos");
		builder.append(":");
		builder.append(equip_pos);
		return builder.toString();
	}
}