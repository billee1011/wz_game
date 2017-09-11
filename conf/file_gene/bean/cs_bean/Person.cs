using System.Collections.Generic;
using System.Text;

public class Person{
	private int id; 

	private string name; 

	private string phoneNum; 

	private int test; 

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

	public string getPhoneNum(){
		return phoneNum;
	}

	public void setPhoneNum(string phoneNum){
		this.phoneNum = phoneNum;
	}

	public int getTest(){
		return test;
	}

	public void setTest(int test){
		this.test = test;
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
		builder.Append("phoneNum");
		builder.Append(":");
		builder.Append(phoneNum);
		builder.Append(",");
		builder.Append("test");
		builder.Append(":");
		builder.Append(test);
		builder.Append(",");
		builder.Append("quality");
		builder.Append(":");
		builder.Append(quality);
		return builder.ToString();
	}
}