using System.Collections.Generic;
using System.Text;

public class Equip{
	private int id; 

	private string name; 

	private List<EAttributeKeyValue> attribute; 

	private EItemQuality quality; 

	private List<EAttributeKeyValue> level_add_attribute; 

	private EEquipPos equip_pos; 

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public string getName(){
		return name;
	}

	public void setName(string name){
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

	public string toString(){
		StringBuilder builder = new StringBuilder();
		builder.Append("id");
		builder.Append(":");
		builder.Append(id);
		builder.Append(",");
		builder.Append("name");
		builder.Append(":");
		builder.Append(name);
		builder.Append(",");
		builder.Append("attribute");
		builder.Append(":");
		builder.Append(attribute);
		builder.Append(",");
		builder.Append("quality");
		builder.Append(":");
		builder.Append(quality);
		builder.Append(",");
		builder.Append("level_add_attribute");
		builder.Append(":");
		builder.Append(level_add_attribute);
		builder.Append(",");
		builder.Append("equip_pos");
		builder.Append(":");
		builder.Append(equip_pos);
		return builder.ToString();
	}
}