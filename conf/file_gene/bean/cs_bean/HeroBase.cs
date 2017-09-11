using System.Collections.Generic;
using System.Text;

public class HeroBase{
	private int id; 

	private string name; 

	private EItemQuality quality; 

	private int intelligence; 

	private EJob job; 

	private ECountry country; 

	private List<int> union_id_list; 

	private List<int> break_id_list; 

	private List<EAttributeKeyValue> battle_attribute; 

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

	public EItemQuality getQuality(){
		return quality;
	}

	public void setQuality(EItemQuality quality){
		this.quality = quality;
	}

	public int getIntelligence(){
		return intelligence;
	}

	public void setIntelligence(int intelligence){
		this.intelligence = intelligence;
	}

	public EJob getJob(){
		return job;
	}

	public void setJob(EJob job){
		this.job = job;
	}

	public ECountry getCountry(){
		return country;
	}

	public void setCountry(ECountry country){
		this.country = country;
	}

	public List<int> getUnion_id_list(){
		return union_id_list;
	}

	public void setUnion_id_list(List<int> union_id_list){
		this.union_id_list = union_id_list;
	}

	public List<int> getBreak_id_list(){
		return break_id_list;
	}

	public void setBreak_id_list(List<int> break_id_list){
		this.break_id_list = break_id_list;
	}

	public List<EAttributeKeyValue> getBattle_attribute(){
		return battle_attribute;
	}

	public void setBattle_attribute(List<EAttributeKeyValue> battle_attribute){
		this.battle_attribute = battle_attribute;
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
		builder.Append("quality");
		builder.Append(":");
		builder.Append(quality);
		builder.Append(",");
		builder.Append("intelligence");
		builder.Append(":");
		builder.Append(intelligence);
		builder.Append(",");
		builder.Append("job");
		builder.Append(":");
		builder.Append(job);
		builder.Append(",");
		builder.Append("country");
		builder.Append(":");
		builder.Append(country);
		builder.Append(",");
		builder.Append("union_id_list");
		builder.Append(":");
		builder.Append(union_id_list);
		builder.Append(",");
		builder.Append("break_id_list");
		builder.Append(":");
		builder.Append(break_id_list);
		builder.Append(",");
		builder.Append("battle_attribute");
		builder.Append(":");
		builder.Append(battle_attribute);
		return builder.ToString();
	}
}