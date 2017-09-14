package bean;

import java.util.List;
import annotation.ListDesc;
import annotation.EnumField;
public class EquipClient{
	private int id; 

	private String name; 

	@ListDesc("EAttributeKeyValueClient")
	private List<EAttributeKeyValueClient> attribute; 

	@EnumField("EItemQuality")
	private int quality; 

	@ListDesc("EAttributeKeyValueClient")
	private List<EAttributeKeyValueClient> level_add_attribute; 

	@EnumField("EEquipPos")
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

	public List<EAttributeKeyValueClient> getAttribute(){
		return attribute;
	}

	public void setAttribute(List<EAttributeKeyValueClient> attribute){
		this.attribute = attribute;
	}

	public int getQuality(){
		return quality;
	}

	public void setQuality(int quality){
		this.quality = quality;
	}

	public List<EAttributeKeyValueClient> getLevel_add_attribute(){
		return level_add_attribute;
	}

	public void setLevel_add_attribute(List<EAttributeKeyValueClient> level_add_attribute){
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