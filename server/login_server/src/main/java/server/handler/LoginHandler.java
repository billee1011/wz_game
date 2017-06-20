package server.handler;

import annotation.Path;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.recource.FastLoginHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/2/6.
 */
public class LoginHandler extends AbstractHandler {


	private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
	private Map<String, ObjectAndMethod> handlerMap = new HashMap<>();

	public LoginHandler() {
		registerAllPath();
	}

	private void registerAllPath() {
		registerOnePath(FastLoginHandler.class);
	}

	private class ObjectAndMethod {
		Object object;
		Method method;

		public ObjectAndMethod(Object object, Method method) {
			this.object = object;
			this.method = method;
		}
	}


	private void registerOnePath(Class<?> classType) {
		Path path = classType.getAnnotation(Path.class);
		if (path != null) {
			Object obj = null;
			try {
				obj = classType.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			Method[] methodList = classType.getDeclaredMethods();
			for (Method method : methodList) {
				Path methodPath = method.getAnnotation(Path.class);
				if (methodPath != null) {
					handlerMap.put(path.value() + methodPath.value(), new ObjectAndMethod(obj, method));
				} else {
					logger.warn(" the method is out of range");
				}
			}
		} else {
			logger.warn("you want register a action handler don't have path");
		}
	}

	@Override
	public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
		ObjectAndMethod objectAndMethod = handlerMap.get(s);

		if (objectAndMethod == null) {
			logger.warn("un handler path ");
			return;
		}
		try {
			objectAndMethod.method.invoke(objectAndMethod.object, httpServletRequest, httpServletResponse);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}


}
