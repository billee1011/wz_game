package bean;

import java.util.List;
import annotation.ListDesc;
import annotation.EnumField;
public class HeroBaseClient{
	private int id; 

	private String name; 

	@EnumField("EItemQuality")
	private int quality; 

	private int intelligence; 

	@EnumField("EJob")
	private int job; 

	@EnumField("ECountry")
	private int country; 

	@ListDesc("int")
	private List<Integer> union_id_list; 

	@ListDesc("int")
	private List<Integer> break_id_list; 

	@ListDesc("EAttributeKeyValueClient")
	private List<EAttributeKeyValueClient> battle_attribute; 

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

	public int getQuality(){
		return quality;
	}

	public void setQuality(int quality){
		this.quality = quality;
	}

	public int getIntelligence(){
		return intelligence;
	}

	public void setIntelligence(int intelligence){
		this.intelligence = intelligence;
	}

	public int getJob(){
		return job;
	}

	public void setJob(int job){
		this.job = job;
	}

	public int getCountry(){
		return country;
	}

	public void setCountry(int country){
		this.country = country;
	}

	public List<Integer> getUnion_id_list(){
		return union_id_list;
	}

	public void setUnion_id_list(List<Integer> union_id_list){
		this.union_id_list = union_id_list;
	}

	public List<Integer> getBreak_id_list(){
		return break_id_list;
	}

	public void setBreak_id_list(List<Integer> break_id_list){
		this.break_id_list = break_id_list;
	}

	public List<EAttributeKeyValueClient> getBattle_attribute(){
		return battle_attribute;
	}

	public void setBattle_attribute(List<EAttributeKeyValueClient> battle_attribute){
		this.battle_attribute = battle_attribute;
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
		builder.append("quality");
		builder.append(":");
		builder.append(quality);
		builder.append(",");
		builder.append("intelligence");
		builder.append(":");
		builder.append(intelligence);
		builder.append(",");
		builder.append("job");
		builder.append(":");
		builder.append(job);
		builder.append(",");
		builder.append("country");
		builder.append(":");
		builder.append(country);
		builder.append(",");
		builder.append("union_id_list");
		builder.append(":");
		builder.append(union_id_list);
		builder.append(",");
		builder.append("break_id_list");
		builder.append(":");
		builder.append(break_id_list);
		builder.append(",");
		builder.append("battle_attribute");
		builder.append(":");
		builder.append(battle_attribute);
		return builder.toString();
	}
}