package cn.edu.hfut.dmic.webcollector.util;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

/**
 * Created by zhoukai1 on 2016/7/14.
 */
public class HttpGet {
    private static final Logger log = LogManager.getLogger(HttpGet.class);

    private static PoolingHttpClientConnectionManager connectionManager = null;
    private static HttpClientBuilder httpBulder = null;


    //private static String IP = "www.meishij.net";
  //  private static int PORT = 80;

    static {
       // HttpHost target = new HttpHost(IP, PORT);
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(8000);
       // connectionManager.setMaxPerRoute(new HttpRoute(target), 50);
        connectionManager.setDefaultMaxPerRoute(20);
        //请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// ssl握手异常
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };
        httpBulder = HttpClients.custom();
        httpBulder.setConnectionManager(connectionManager);
        httpBulder.setRetryHandler(httpRequestRetryHandler);

    }

    public static CloseableHttpClient getConnection() {
        CloseableHttpClient httpClient = httpBulder.build();
//        httpClient = httpBulder.build();
        return httpClient;
    }

    public static void closeConnection() {
        connectionManager.closeExpiredConnections();
    }

    public static HttpUriRequest getRequestMethod(Map<String, String> map, String url, String method) {
        //设置http的状态参数
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(Config.TIMEOUT_READ)
                .setConnectTimeout(Config.TIMEOUT_CONNECT)
                .setConnectionRequestTimeout(Config.TIMEOUT_CONNECT)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .build();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> e : entrySet) {
            String name = e.getKey();
            String value = e.getValue();
            NameValuePair pair = new BasicNameValuePair(name, value);
            params.add(pair);
        }
        HttpUriRequest reqMethod = null;
        if ("post".equals(method)) {
            reqMethod = RequestBuilder.post().setUri(url)
                    .addParameters(params.toArray(new BasicNameValuePair[params.size()]))
                    .setConfig(requestConfig).build();
        } else if ("get".equals(method)) {
            reqMethod = RequestBuilder.get().setUri(url)
                    .addParameters(params.toArray(new BasicNameValuePair[params.size()]))
                    .setConfig(requestConfig).build();
        }
        return reqMethod;
    }

    public static String getProxy(String proxyUrl) {
        for (int i = 0; i < 10; i++) {
            try {
                //设置http的状态参数
                HttpClient client = getConnection();
                RequestConfig requestConfig = RequestConfig.custom()
                        .setSocketTimeout(Config.TIMEOUT_READ)
                        .setConnectTimeout(Config.TIMEOUT_CONNECT)
                        .setConnectionRequestTimeout(Config.TIMEOUT_CONNECT)
                        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                        .build();
                String url = "http://" + proxyUrl + "/ProxyService/proxy";
                HttpUriRequest reqMethod = RequestBuilder.get().setUri(url).setConfig(requestConfig).build();
                HttpResponse response = client.execute(reqMethod);
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    String message = EntityUtils.toString(entity, "utf-8");
                    message = message.substring(1, message.length() - 1);
                    String ip = message.split(", ")[0].split(":")[1];
                    String port = message.split(", ")[1].split(":")[1];
                    return ip + ":" + port;
                } else {
                    continue;
                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                continue;
            }
        }
        return "";
    }

    public static void timerClearConnection() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                HttpGet.closeConnection();
            }
        }, 1000, 200000);
    }

    public static void main(String args[]) throws IOException {

        getProxy("119.29.40.82:8087");


        Map<String, String> map = new HashMap<String, String>();
        // map.put("account", "");
        // map.put("password", "");

        HttpClient client = getConnection();
        HttpUriRequest post = getRequestMethod(map, "http://www.meishij.net/", "get");
        HttpResponse response = client.execute(post);

        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            String message = EntityUtils.toString(entity, "utf-8");
            System.out.println(message);
        } else {
            System.out.println("请求失败");
        }
    }


}
