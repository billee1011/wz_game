package util;

import java.util.HashSet;
import java.util.Set;

public class WordBadUtil {
	public static final String DEFAULT_PROVINCE = "火星";
	public static final String DEFAULT_CITY = "火星";
	
	private static Set<String> badProvince = new HashSet<>();
	
	static {
		badProvince.add("柬埔寨");
		badProvince.add("新加坡");
		badProvince.add("局域网");
	}

	public static boolean hasBadProvince(String province) {
		if(province == null || "".equals(province)){
			return false;
		}
		
		for (String s : badProvince) {
			if(province.contains(s)){
				return true;
			}
		}
		
		return false;
	}
	
	public static void main(String[] args) {
		System.out.println(WordBadUtil.hasBadProvince("柬埔寨  00"));
	}

}
