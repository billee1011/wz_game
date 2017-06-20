package util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Created by think on 2017/3/29.
 */
public class JsonToLua {

	public static String convert(String srcJson) {
		StringBuilder builder = new StringBuilder();
		Gson gson = new Gson();
		JsonObject jsonObject = gson.fromJson(srcJson, JsonObject.class);
		if (jsonObject == null) {
			throw new RuntimeException("illegal json string ");
		}

		return builder.toString();
	}


}
