package util;

import com.google.gson.Gson;
import com.google.protobuf.Message;
import com.google.protobuf.MessageLite;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

public class LogUtil {
	public static final Logger msgLogger = LoggerFactory.getLogger("message");

	public static void logLogger(Logger logger, String tab, String... params) {

		String data_context = "";
		for (int i = 0; i < params.length; i++) {
			data_context += (params[i] + ",");
		}

		logger.info("******** {} | {}", tab, data_context);
	}

	public static void logLogger(Logger logger, String tab, MessageLite message, String... params) {

		String data_context = "";
		for (int i = 0; i < params.length; i++) {
			data_context += (params[i] + ",");
		}

		logger.info("######## {} | {} | {}", tab, data_context, new Gson().toJson(message.toString()));
	}

	public static void logHttp(Logger logger, String s, Request request) {
		Enumeration enu=request.getParameterNames();
		StringBuffer sb = new StringBuffer();
		sb.append("api_name: " + s);
		while(enu.hasMoreElements()){
			String paraName=(String)enu.nextElement();
			sb.append("   " + paraName +": "+request.getParameter(paraName));
//			logger.info("api_name:" + s + "  " + paraName+": "+request.getParameter(paraName));
		}
		logger.info(sb.toString());
	}
	
	public static void error(Logger logger,Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		logger.error(sw.toString());
	}
}
