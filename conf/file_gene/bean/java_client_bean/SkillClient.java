package bean;

import java.util.List;
import annotation.ListDesc;
public class SkillClient{
	private int id; 

	private String name; 

	private int skill_type; 

	private int skill_target; 

	private int before; 

	private int after; 

	private int ratio; 

	private String desc; 

	@ListDesc("int")
	private List<Integer> composite_hero; 

	private int cast_hero; 

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

	public int getSkill_type(){
		return skill_type;
	}

	public void setSkill_type(int skill_type){
		this.skill_type = skill_type;
	}

	public int getSkill_target(){
		return skill_target;
	}

	public void setSkill_target(int skill_target){
		this.skill_target = skill_target;
	}

	public int getBefore(){
		return before;
	}

	public void setBefore(int before){
		this.before = before;
	}

	public int getAfter(){
		return after;
	}

	public void setAfter(int after){
		this.after = after;
	}

	public int getRatio(){
		return ratio;
	}

	public void setRatio(int ratio){
		this.ratio = ratio;
	}

	public String getDesc(){
		return desc;
	}

	public void setDesc(String desc){
		this.desc = desc;
	}

	public List<Integer> getComposite_hero(){
		return composite_hero;
	}

	public void setComposite_hero(List<Integer> composite_hero){
		this.composite_hero = composite_hero;
	}

	public int getCast_hero(){
		return cast_hero;
	}

	public void setCast_hero(int cast_hero){
		this.cast_hero = cast_hero;
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
		builder.append("skill_type");
		builder.append(":");
		builder.append(skill_type);
		builder.append(",");
		builder.append("skill_target");
		builder.append(":");
		builder.append(skill_target);
		builder.append(",");
		builder.append("before");
		builder.append(":");
		builder.append(before);
		builder.append(",");
		builder.append("after");
		builder.append(":");
		builder.append(after);
		builder.append(",");
		builder.append("ratio");
		builder.append(":");
		builder.append(ratio);
		builder.append(",");
		builder.append("desc");
		builder.append(":");
		builder.append(desc);
		builder.append(",");
		builder.append("composite_hero");
		builder.append(":");
		builder.append(composite_hero);
		builder.append(",");
		builder.append("cast_hero");
		builder.append(":");
		builder.append(cast_hero);
		return builder.toString();
	}
}