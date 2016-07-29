package cn.edu.hfut.dmic.webcollector.util;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.util.model.City;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebCollector抓取图片的例子
 *
 * @author hu
 */
public class Crawler12306 extends BreadthCrawler {

    private Proxys proxys;

    /**
     * @param crawlPath    用于维护URL的文件夹
     * @param downloadPath 用于保存图片的文件夹
     */
    public Crawler12306(String crawlPath, String downloadPath) {
        super(crawlPath, true);
        proxys = new Proxys();
        proxys.add("119.29.40.82", 8087);
        proxys.add("119.29.40.82", 8083);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        String html = page.getHtml();
        if (StringUtils.isBlank(html)) {
            return;
        }
        html = html.replace("var station_names ='", "");
        html = html.substring(html.indexOf("@") + 1, html.length() - 2).trim();
        html = html.replaceAll("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "");
        System.out.println(html);
        List<City> cities = new ArrayList<City>();
        for (String cityStr : html.split("@")) {
            if ("".equals(cityStr)) {
                continue;
            }
            String[] strs = cityStr.split("\\|");
            City city = new City(strs[0], strs[1], strs[2], strs[3],strs[4],strs[5]);
            cities.add(city);
        }
        for (City city : cities) {
            System.out.println(city);
        }
    }


    public static void main(String[] args) throws Exception {
        Crawler12306 demoImageCrawler = new Crawler12306("crawl-12306", "download-12306");
        //添加种子URL
        demoImageCrawler.addSeed("https://kyfw.12306.cn/otn/resources/js/framework/station_name.js?station_version=1.8959");
        //设置为断点爬取，否则每次开启爬虫都会重新爬取
        demoImageCrawler.setResumable(false);
        demoImageCrawler.setThreads(30);
        Config.MAX_RECEIVE_SIZE = 1000 * 1000 * 10;
        demoImageCrawler.start(100);
    }

    public static String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in)))
            return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught
            // here; it should not happen.
            if ((current == 0x9) || (current == 0xA) || (current == 0xD)
                    || ((current >= 0x20) && (current <= 0xD7FF))
                    || ((current >= 0xE000) && (current <= 0xFFFD))
                    || ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

    @Override
    public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
        HttpRequest request = new HttpRequest(crawlDatum);
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Accept", "*/*");
        request.setHeader("Accept-Encoding", "gzip, deflate, sdch");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Pragma", "no-cache");
        request.setHeader("Host", "kyfw.12306.cn");
        request.setHeader("Referer", "https://kyfw.12306.cn/otn/lcxxcx/init");
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
        request.setProxy(getRealProxy());
        return request.getResponse();
    }

    public Proxy getRealProxy() {
        Proxy proxy = proxys.nextRandom();
        String address = proxy.address().toString();
        String realAddress = cn.edu.hfut.dmic.webcollector.util.HttpGet.getProxy(address.substring(1));
        String ip = realAddress.split(":")[0];
        int port = Integer.parseInt(realAddress.split(":")[1]);
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        return proxy;
    }
}