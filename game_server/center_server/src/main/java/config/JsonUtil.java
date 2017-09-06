package config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import config.provider.BaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class JsonUtil {

	private static ThreadLocal<Gson> gsonList = new ThreadLocal<>();

	public static <T> Map<Integer, T> getJsonMap(Class<T[]> classType, String jsonName) {
		Map<Integer, T> resultMap = new HashMap<>();
		T[] result = getGson().fromJson(getJsonString(jsonName), classType);
		for (T t : result) {
			if (t instanceof IConfParseBean) {
				IConfParseBean bean = (IConfParseBean) t;
				bean.parse();
				resultMap.put(bean.getId(), (T) bean);
			}
		}
		return resultMap;
	}

	public static Gson getGson() {
		Gson gson = gsonList.get();
		if (gson == null) {
			gson = new Gson();
			gsonList.set(gson);
		}
		return gson;
	}

	public static String getJsonString(String jsonName) {
		InputStream in = null;
		try {
			in = new FileInputStream(BaseProvider.CONF_PATH + jsonName);
			int length = in.available();
			byte[] bytes = new byte[length];
			in.read(bytes, 0, length);
			return new String(bytes, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getJsonString(Object object) {
		Gson gson = new Gson();
		return gson.toJson(object).toString();
	}

	public static JsonObject getJsonObject(HttpServletRequest request) {
		String bodyStr = getRequestBody(request);
		Gson gson = new Gson();
		return gson.fromJson(bodyStr, JsonObject.class);
	}


	public static String getRequestBody(HttpServletRequest request) {
		int length = 0;
		InputStream in = null;
		try {
			in = request.getInputStream();
			length = in.available();
			byte[] bytes = new byte[length];
			in.read(bytes);
			return new String(bytes, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
