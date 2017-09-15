package inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Created by wangfang on 2017/9/15.
 */
public class BeanManager {
	private static Injector injector;

	public static void init() {
		injector = Guice.createInjector(new RyModule());
	}

	public static <T> T getBean(Class<T> classType) {
		return injector.getInstance(classType);
	}

}
