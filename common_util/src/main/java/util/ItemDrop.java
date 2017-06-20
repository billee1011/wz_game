package util;

public class ItemDrop {
	private int type;
	private int id;
	private int count;

	public ItemDrop() {

	}

	public ItemDrop(int type, int id, int count) {
		this.type = type;
		this.id = id;
		this.count = count;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
