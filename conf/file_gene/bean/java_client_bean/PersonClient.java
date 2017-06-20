package bean;

public class PersonClient{
	private int id; 

	private String name; 

	private String phoneNum; 

	private int test; 

	private EItemQuality quality; 

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

	public String getPhoneNum(){
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum){
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
		builder.append("phoneNum");
		builder.append(":");
		builder.append(phoneNum);
		builder.append(",");
		builder.append("test");
		builder.append(":");
		builder.append(test);
		builder.append(",");
		builder.append("quality");
		builder.append(":");
		builder.append(quality);
		return builder.toString();
	}
}