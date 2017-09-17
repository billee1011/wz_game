using System.Collections.Generic;
using System.Text;

public class Hero_union{
	private int id; 

	private string union_name; 

	private int type; 

	private List<EAttributeKeyValue> attr_list; 

	private List<int> actvice_condition; 

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public string getUnion_name(){
		return union_name;
	}

	public void setUnion_name(string union_name){
		this.union_name = union_name;
	}

	public int getType(){
		return type;
	}

	public void setType(int type){
		this.type = type;
	}

	public List<EAttributeKeyValue> getAttr_list(){
		return attr_list;
	}

	public void setAttr_list(List<EAttributeKeyValue> attr_list){
		this.attr_list = attr_list;
	}

	public List<int> getActvice_condition(){
		return actvice_condition;
	}

	public void setActvice_condition(List<int> actvice_condition){
		this.actvice_condition = actvice_condition;
	}

	public string toString(){
		StringBuilder builder = new StringBuilder();
		builder.Append("id");
		builder.Append(":");
		builder.Append(id);
		builder.Append(",");
		builder.Append("union_name");
		builder.Append(":");
		builder.Append(union_name);
		builder.Append(",");
		builder.Append("type");
		builder.Append(":");
		builder.Append(type);
		builder.Append(",");
		builder.Append("attr_list");
		builder.Append(":");
		builder.Append(attr_list);
		builder.Append(",");
		builder.Append("actvice_condition");
		builder.Append(":");
		builder.Append(actvice_condition);
		return builder.ToString();
	}
}