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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * WebCollector抓取图片的例子
 *
 * @author hu
 */
public class BaiDuSpider extends BreadthCrawler {

    private Proxys proxys;

    /**
     * @param crawlPath    用于维护URL的文件夹
     * @param downloadPath 用于保存图片的文件夹
     */
    public BaiDuSpider(String crawlPath, String downloadPath) {
        super(crawlPath, true);
        proxys = new Proxys();
        proxys.add("119.29.40.82", 8087);
        proxys.add("119.29.40.82", 8083);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        Elements results = page.select("div#content_left > div.result");
        for(Element resultEle : results){
            String url =resultEle.select("h3.t > a").get(0).attr("href");
            try {
                HttpRequest request = new HttpRequest(url);
                request.setHeader("Connection", "keep-alive");
                request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");
                request.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
                request.setHeader("Cache-Control", "no-cache");
                request.setHeader("Pragma", "no-cache");
                request.setHeader("Host", "www.baidu.com");
                request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
                request.setProxy(getRealProxy());
                HttpResponse response =  request.getResponse();
                String realUrl=  response.getRealUrl().getPath();
                System.out.println(realUrl);

            }catch (Exception e){
                LOG.error(e.getMessage(),e);
            }

            String title=resultEle.select("h3.t").get(0).text();
            String content=resultEle.select("div.c-abstract").get(0).text();
            System.out.println(url);
        }
    }


    public static void main(String[] args) throws Exception {
        BaiDuSpider demoImageCrawler = new BaiDuSpider("crawl-baidu", "download-baidu");
        //添加种子URL
        demoImageCrawler.addSeed("https://www.baidu.com/s?ie=utf-8tn=baidu&wd=%E5%BC%80%E6%BA%90%E4%B8%AD%E5%9B%BD");
        //设置为断点爬取，否则每次开启爬虫都会重新爬取
        demoImageCrawler.setResumable(false);
        demoImageCrawler.setThreads(30);
        Config.MAX_RECEIVE_SIZE = 1000 * 1000 * 10;
        demoImageCrawler.start(10);
    }


    @Override
    public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
        HttpRequest request = new HttpRequest(crawlDatum);
        request.setHeader("Connection", "keep-alive");
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");
        request.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
        request.setHeader("Cache-Control", "no-cache");
        request.setHeader("Pragma", "no-cache");
        request.setHeader("Host", "www.baidu.com");
        request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
        request.setProxy(getRealProxy());
        return request.getResponse();
    }

    public Proxy getRealProxy() {
        Proxy proxy = proxys.nextRandom();
        String address = proxy.address().toString();
        String realAddress = HttpGet.getProxy(address.substring(1));
        String ip = realAddress.split(":")[0];
        int port = Integer.parseInt(realAddress.split(":")[1]);
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        return proxy;
    }
}