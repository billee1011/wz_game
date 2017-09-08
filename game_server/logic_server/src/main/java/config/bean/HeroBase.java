package config.bean;

import annotation.ListDesc;
import config.IConfParseBean;

import java.util.List;

public class HeroBase implements IConfParseBean {
	private int id; 

	private String name; 

	private EItemQuality quality; 

	private int intelligence; 

	private EJob job; 

	private ECountry country; 

	@ListDesc("int")
	private List<Integer> union_id_list; 

	@ListDesc("int")
	private List<Integer> break_id_list; 

	@ListDesc("EAttributeKeyValue")
	private List<EAttributeKeyValue> battle_attribute; 

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

	public List<EAttributeKeyValue> getBattle_attribute(){
		return battle_attribute;
	}

	public void setBattle_attribute(List<EAttributeKeyValue> battle_attribute){
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

	@Override
	public boolean parse() {
		return false;
	}
}