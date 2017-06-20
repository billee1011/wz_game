package test;

import bean.Hello;
import bean.IConfig;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class JsonUtil {

	private static ThreadLocal<Gson> gsonList = new ThreadLocal<>();

	public static <T> Map<Integer, T> getJsonMap(Class<T[]> classType, String jsonStr) {
		Map<Integer, T> resultMap = new HashMap<>();
		T[] result = getGson().fromJson(jsonStr, classType);
		for (T t : result) {
			if (t instanceof IConfig) {
				IConfig bean = (IConfig) t;
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


	public static void main(String[] args) throws Exception {
		String path = System.getProperty("user.dir") + File.separator + ".." + File.separator + "conf_file" + File.separator + "server" + File.separator + "Hello.json";
		String str = new BufferedReader(new InputStreamReader(new FileInputStream(path))).readLine();
		JsonUtil.getJsonMap(Hello[].class, str).forEach((e, f) -> System.out.println("the result is " + f));
	}

}
