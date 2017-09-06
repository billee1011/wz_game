package chr;

import java.util.Map;

public class SaveObject {
		public int playerId;
		public String tabName;
		public Map<String, Object> data;
		public Map<String, Object> where;

		public SaveObject(int playerId, String tabName, Map<String, Object> data, Map<String, Object> where) {
			this.playerId = playerId;
			this.tabName = tabName;
			this.data = data;
			this.where = where;
		}
		
		public SaveObject(int playerId, String tabName, Map<String, Object> data) {
			this.playerId = playerId;
			this.tabName = tabName;
			this.data = data;
		}
}