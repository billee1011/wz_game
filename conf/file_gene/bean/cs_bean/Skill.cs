using System.Collections.Generic;
using System.Text;

public class Skill{
	private int id; 

	private string name; 

	private ESkillType skill_type; 

	private ESkillTarget skill_target; 

	private int before; 

	private int after; 

	private int ratio; 

	private string desc; 

	private List<int> composite_hero; 

	private int cast_hero; 

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

	public ESkillType getSkill_type(){
		return skill_type;
	}

	public void setSkill_type(ESkillType skill_type){
		this.skill_type = skill_type;
	}

	public ESkillTarget getSkill_target(){
		return skill_target;
	}

	public void setSkill_target(ESkillTarget skill_target){
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

	public string getDesc(){
		return desc;
	}

	public void setDesc(string desc){
		this.desc = desc;
	}

	public List<int> getComposite_hero(){
		return composite_hero;
	}

	public void setComposite_hero(List<int> composite_hero){
		this.composite_hero = composite_hero;
	}

	public int getCast_hero(){
		return cast_hero;
	}

	public void setCast_hero(int cast_hero){
		this.cast_hero = cast_hero;
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
		builder.Append("skill_type");
		builder.Append(":");
		builder.Append(skill_type);
		builder.Append(",");
		builder.Append("skill_target");
		builder.Append(":");
		builder.Append(skill_target);
		builder.Append(",");
		builder.Append("before");
		builder.Append(":");
		builder.Append(before);
		builder.Append(",");
		builder.Append("after");
		builder.Append(":");
		builder.Append(after);
		builder.Append(",");
		builder.Append("ratio");
		builder.Append(":");
		builder.Append(ratio);
		builder.Append(",");
		builder.Append("desc");
		builder.Append(":");
		builder.Append(desc);
		builder.Append(",");
		builder.Append("composite_hero");
		builder.Append(":");
		builder.Append(composite_hero);
		builder.Append(",");
		builder.Append("cast_hero");
		builder.Append(":");
		builder.Append(cast_hero);
		return builder.ToString();
	}
}