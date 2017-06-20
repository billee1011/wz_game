package util;

import org.apache.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by think on 2017/6/14.
 */
public class HttpUtil {
    public static void writeResponse(HttpServletResponse response, String content) {
        response.setStatus(HttpStatus.SC_OK);
        try {
            response.getWriter().write(content);
            response.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
