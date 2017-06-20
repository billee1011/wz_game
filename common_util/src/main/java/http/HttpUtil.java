package http;

import actor.ICallback;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/12/7.
 */
public class HttpUtil {

	public static String sendPostSync(String url, String content) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(content.getBytes()));
		reqEntity.setContentType("application/xml");
		reqEntity.setContentEncoding("utf-8");
		reqEntity.setChunked(true);
		httpPost.setEntity(reqEntity);
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
		} catch (IOException e) {
			e.printStackTrace();
			return "failed";
		}
		HttpEntity resEntity = response.getEntity();
		try {
			String result = EntityUtils.toString(resEntity, "utf-8");
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return "failed";
		}
	}

	public static void sendPost(String url, Map<String, String> params, ICallback callback) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httpost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			nvps.add(new BasicNameValuePair(key, params.get(key)));
		}
		try {
			httpost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		HttpResponse response = null;
		try {
			response = httpclient.execute(httpost);
		} catch (Exception e) {
			e.printStackTrace();
			callback.onResult(e);
			return;
		}
		HttpEntity entity = response.getEntity();
		String charset = EntityUtils.getContentCharSet(entity);
		String body = null;
		try {
			body = EntityUtils.toString(entity);
		} catch (Exception e) {
			e.printStackTrace();
			callback.onResult(e);
			return;
		}
		callback.onResult(body);
	}

	public static void sendPost(String url, String content, ICallback callback) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(content.getBytes()));
		reqEntity.setContentType("application/json");
		reqEntity.setContentEncoding("utf-8");
		reqEntity.setChunked(true);
		httpPost.setEntity(reqEntity);
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			HttpEntity resEntity = response.getEntity();
			String result = EntityUtils.toString(resEntity, "utf-8");
			callback.onResult(result);
		} catch (IOException e) {
			e.printStackTrace();
			callback.onResult(e);
		}
	}
}
