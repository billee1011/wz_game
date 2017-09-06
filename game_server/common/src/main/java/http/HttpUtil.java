package http;

import actor.Actor;
import actor.ActorDispatcher;
import actor.IActor;
import actor.ICallback;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import timer.ActTimer;
import util.MiscUtil;
import util.Randomizer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2016/12/7.
 */
public class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static HttpClient getHttpClient() {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
        return client;
    }

    public static String sendPostSync(String url, String content) {
        HttpClient httpClient = getHttpClient();
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

    public static String sendPost(String url, Map<String, String> params) {
        HttpClient httpclient = getHttpClient();
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
            return "error";
        }
        HttpEntity entity = response.getEntity();
        String charset = EntityUtils.getContentCharSet(entity);
        String body = null;
        try {
            body = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return body;
    }

    public static void sendPost(String url, Map<String, String> params, ICallback callback) {
        logger.info(" the post url is {}", url);
        HttpClient httpclient = getHttpClient();
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
            if (callback != null) {
                callback.onResult(e);
            }
            return;
        }
        HttpEntity entity = response.getEntity();
        String charset = EntityUtils.getContentCharSet(entity);
        String body = null;
        try {
            body = EntityUtils.toString(entity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onResult(e);
            }
            return;
        }
        if (callback != null) {
            callback.onResult(body);
        }
    }


    public static void sendPost(String url, String content, ICallback callback) {
        HttpClient httpClient = getHttpClient();
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
            logger.info("the result is {}", result);
            if (callback != null) {
                callback.onResult(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onResult(e);
            }
        }
    }

    private static int updateCount = 0;

    public static void main(String[] args) {
        ActorDispatcher dispatcher = new ActorDispatcher(80, "test");
        ActTimer timer = new ActTimer("check");
        timer.start();
        IActor actor = new Actor("HEHE");
        actor.start();
        dispatcher.start();
        timer.register(5000, 5000, () -> {
            updateCount++;
            logger.info("send post count {} average cost time {}", sendCount.get(), 5000 * updateCount / sendCount.get());
        }, actor, " log status ");
    }


    private static AtomicInteger sendCount = new AtomicInteger(0);
    private static AtomicLong costTime = new AtomicLong(0);

    private static void testSendPost() {
        long timestamp = System.currentTimeMillis();
        String channel = "CS_WX_QRCODE";  //CS_ALI_QRCODE CS_GATEWAY CS_WX_QRCODE
        int bill_timeout = 3000;
        String title = "购买商品";
        int total_fee = 1;
        String bill_no = createBillNo();
        String appId = "71719d32-77c2-4ca3-b9ec-24c252a58bb1";
        String appKey = "255fb7b7-546e-4a16-955e-c9254417bc22";
        String appSign = MiscUtil.getMD5(appId + timestamp + appKey);
        String bank = "10001111111";
        JsonObject obj = new JsonObject();
        obj.addProperty("app_id", appId);
        obj.addProperty("channel", channel);
        obj.addProperty("app_sign", appSign);
        obj.addProperty("bill_timeout", bill_timeout);
        obj.addProperty("bill_no", bill_no);
        obj.addProperty("bank", bank);
        obj.addProperty("title", title);
        obj.addProperty("real_ip", "120.237.123.242");
        obj.addProperty("total_fee", total_fee);
        obj.addProperty("timestamp", timestamp);
        obj.addProperty("return_url", "http://localhost");
        String gsonString = obj.toString();
        sendCount.incrementAndGet();
        HttpUtil.sendPost("http://127.0.0.1:8080/2/rest/bill", gsonString, null);
    }

    static String createBillNo() {
        String prefix = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        long randomNum = Math.round(Math.random() * 1000);
        String billNo = prefix + randomNum;
        return billNo;
    }

    public static String sendGet(String url) {
        HttpClient httpClient = getHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
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
}
