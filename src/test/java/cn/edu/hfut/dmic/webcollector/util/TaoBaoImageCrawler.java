package cn.edu.hfut.dmic.webcollector.util;

import cn.edu.hfut.dmic.webcollector.fetcher.Executor;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpRequest;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.net.Proxys;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebCollector抓取图片的例子
 *
 * @author hu
 */
public class TaoBaoImageCrawler extends BreadthCrawler {

    private Proxys proxys;

    //用于保存图片的文件夹
    private File downloadDir;

    //原子性int，用于生成图片文件名
    private AtomicInteger imageId;

    /**
     * @param crawlPath    用于维护URL的文件夹
     * @param downloadPath 用于保存图片的文件夹
     */
    public TaoBaoImageCrawler(String crawlPath, String downloadPath) {
        super(crawlPath, true);
        downloadDir = new File(downloadPath);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        computeImageId();
        proxys = new Proxys();
        proxys.add("119.29.40.82", 8087);
        proxys.add("119.29.40.82", 8083);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        //根据http头中的Content-Type信息来判断当前资源是网页还是图片
        String contentType = page.getResponse().getContentType();
        if (contentType == null) {
            return;
        } else if (contentType.contains("html")) {
            //如果是网页，则抽取其中包含图片的URL，放入后续任务
            Elements imgs = page.select("img[src]");
            for (Element img : imgs) {
                String imgSrc = img.attr("abs:src");
                next.add(imgSrc);
            }

        } else if (contentType.startsWith("image")) {
            //如果是图片，直接下载
            String extensionName = contentType.split("/")[1];
            String imageFileName = imageId.incrementAndGet() + "." + extensionName;
            File imageFile = new File(downloadDir, imageFileName);
            try {
                FileUtils.writeFile(imageFile, page.getContent());
                System.out.println("保存图片 " + page.getUrl() + " 到 " + imageFile.getAbsolutePath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }


    public static void main(String[] args) throws Exception {

        cn.edu.hfut.dmic.webcollector.util.HttpGet.timerClearConnection();

          /*定制Executor*/
        Executor executor = new Executor() {

            /*execute应该包含对一个页面从http请求到抽取的过程
              如果在execute中发生异常并抛出，例如http请求超时，
              爬虫会在后面的任务中继续爬取execute失败的任务。
              如果一个任务重试次数太多，超过Config.MAX_EXECUTE_COUNT，
              爬虫会忽略这个任务。Config.MAX_EXECUTE_COUNT的值可以被修改*/
            public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
                CloseableHttpClient client = HttpClients.createDefault();
                String url = datum.getUrl();
                try {
                    HttpGet request = new HttpGet(url);
                    request.setHeader(":authority", "s.taobao.com");
                    request.setHeader("method", "GET");
                    request.setHeader("path", url.replaceAll("https://s.taobao.com", ""));
                    request.setHeader(":scheme", "https");
                    request.setHeader(":accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                    request.setHeader("accept-encoding", "gzip, deflate, sdch, br");
                    request.setHeader("accept-language", "zh-CN,zh;q=0.8");
                    request.setHeader("cache-control", "no-cache");
                    request.setHeader("pragma", "no-cache");
                    request.setHeader("referer", "https://www.taobao.com/");
                    request.setHeader("upgrade-insecure-requests", "1");
                    request.setHeader("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
                    org.apache.http.HttpResponse response = client.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        HttpEntity entity = response.getEntity();
                    /*利用HttpClient获取网页的字节数组，
                      通过CharsetDetector判断网页的编码 */
                        byte[] content = EntityUtils.toByteArray(entity);
                        String charset = CharsetDetector.guessEncoding(content);
                        String html = new String(content, charset);
                        Document doc = Jsoup.parse(html, url);
                        Elements links = doc.select("a[href]");
                        for (int i = 0; i < links.size(); i++) {
                            Element link = links.get(i);
                             /*抽取超链接的绝对路径*/
                            String href = link.attr("abs:href");
                            next.add(href);
                        }
                    } else {
                        throw new Exception(statusCode + "");
                    }
                } finally {
                    client.close();
                }
            }
        };


        TaoBaoImageCrawler demoImageCrawler = new TaoBaoImageCrawler("crawl-dangdang", "download-dangdang");
        //添加种子URL
        Links links = new Links();
        links.add("http://book.dangdang.com/study");
        links.add("http://book.dangdang.com/01.03.htm");
        links.add("http://book.dangdang.com/01.22.htm");
        links.add("http://book.dangdang.com/01.21.htm");
        links.add("http://book.dangdang.com/01.18.htm");
        demoImageCrawler.addSeed(links);
        //demoImageCrawler.addSeed("http://www.meishichina.com/");
        //限定爬取范围
        demoImageCrawler.addRegex("http://book.dangdang.com/.*");
        //设置为断点爬取，否则每次开启爬虫都会重新爬取
        demoImageCrawler.setResumable(false);
        demoImageCrawler.setThreads(30);
        //  demoImageCrawler.setExecutor(executor);
//        demoImageCrawler.setExecuteInterval(3000);
        Config.MAX_RECEIVE_SIZE = 1000 * 1000 * 10;
        Config.TIMEOUT_READ = 30000;
        Config.TIMEOUT_CONNECT = 10000;
        demoImageCrawler.start(100);
        System.exit(0);
    }

    public void computeImageId() {
        int maxId = -1;
        for (File imageFile : downloadDir.listFiles()) {
            String fileName = imageFile.getName();
            String idStr = fileName.split("\\.")[0];
            int id = Integer.valueOf(idStr);
            if (id > maxId) {
                maxId = id;
            }
        }
        imageId = new AtomicInteger(maxId);
    }

    @Override
    public HttpResponse getResponse(CrawlDatum crawlDatum) throws Exception {
        HttpRequest request = new HttpRequest(crawlDatum);
        request.setHeader("Connection", "keep-alive");
        request.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader("accept-encoding", "gzip, deflate, sdch");
        request.setHeader("accept-language", "zh-CN,zh;q=0.8");
        request.setHeader("cache-control", "no-cache");
        request.setHeader("pragma", "no-cache");
        request.setHeader("Host", "book.dangdang.com");
        request.setHeader("Referer","http://book.dangdang.com/");
        request.setHeader("upgrade-insecure-requests", "1");
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