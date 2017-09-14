package bean;

import java.util.List;
import annotation.ListDesc;
import annotation.EnumField;
public class Equip{
	private int id; 

	private String name; 

	@ListDesc("EAttributeKeyValue")
	private List<EAttributeKeyValue> attribute; 

	private EItemQuality quality; 

	@ListDesc("EAttributeKeyValue")
	private List<EAttributeKeyValue> level_add_attribute; 

	private EEquipPos equip_pos; 

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

	public EItemQuality getQuality(){
		return quality;
	}

	public void setQuality(EItemQuality quality){
		this.quality = quality;
	}

	public List<EAttributeKeyValue> getLevel_add_attribute(){
		return level_add_attribute;
	}

	public void setLevel_add_attribute(List<EAttributeKeyValue> level_add_attribute){
		this.level_add_attribute = level_add_attribute;
	}

	public EEquipPos getEquip_pos(){
		return equip_pos;
	}

	public void setEquip_pos(EEquipPos equip_pos){
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