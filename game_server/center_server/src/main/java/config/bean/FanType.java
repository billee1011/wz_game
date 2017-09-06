package config.bean;

/**
 * Created by Administrator on 2016/12/2.
 */
public class FanType {
	private int id;
	private String name;
	private String icon;
	private boolean show;
	private int fanshu;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isShow() {
		return show;
	}

	public void setShow(boolean show) {
		this.show = show;
	}

	public int getFanshu() {
		return fanshu;
	}

	public void setFanshu(int fanshu) {
		this.fanshu = fanshu;
	}
}
