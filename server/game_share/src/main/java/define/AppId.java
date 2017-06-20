package define;

/**
 * Created by Administrator on 2017/1/12.
 */
public enum AppId {
	LOGIN(1, "login"),                        //登录服务器
	LOGIC(2, "logic"),                        //游戏逻辑服务器
	LOBBY(3, "lobby"),                        //大厅服务器
	CENTER(4, "center"),                      //战区服务器
	GATE(5, "gate"),                          //网关服务器
	DATABASE(6, "database"),                 //数据服务器
	LOG(7, "log"),                            //日志服务
	WORLD(8, "world"),                            //世界服务
	CLIENT(9,"client"),						//客户端
	;

	AppId(int id, String desc) {
		this.id = id;
		this.desc = desc;
	}

	private int id;

	private String desc;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public static AppId getByValue(int value) {
		for (AppId id : values()) {
			if (id.getId() == value) {
				return id;
			}
		}
		return null;
	}
}
