using System.Collections.Generic;
using System.Text;

public class Equip{
	private int id; 

	private string name; 

	private int attribute; 

	private EItemQuality quality; 

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
		return builder.ToString();
	}
}