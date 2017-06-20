package server.recource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by think on 2017/5/18.
 */
public interface ActionHandler {
	void handleAction(HttpServletRequest request, HttpServletResponse response);
}
