package chr.bag;

import chr.RyCharacter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by think on 2017/9/18.
 */
public class CharBag {
	private RyCharacter ch;

	private Map<Integer, Integer> itemMap;

	public CharBag(RyCharacter ch) {
		this.ch = ch;
		itemMap = new HashMap<>();
	}

	public void addItem(int id, int count) {
		itemMap.merge(id, count, (e, f) -> e == null ? 0 : e + f);
	}

	public void removeItem(int id, int count) {
		itemMap.merge(id, count, (e, f) -> e == null ? 0 : e > f ? e - f : 0);
	}
}
