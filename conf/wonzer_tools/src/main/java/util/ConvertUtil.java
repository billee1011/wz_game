package util;

/**
 * Created by think on 2017/5/9.
 */
public class ConvertUtil {

	public static int safe2Int(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static long safe2Long(String value) {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static float safe2Float(String value) {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
