package cn.edu.hfut.dmic.webcollector.util;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by zhoukai1 on 2016/5/11.
 */
public class Demo extends BreadthCrawler {

    public Demo(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
        String str = page.getHtml();
       // JSONObject json = new JSONObject(jsonStr);
        System.out.println("信息：" + str);
    }

    public static void main(String[] args) throws Exception {
        Demo crawler = new Demo("json_crawler", false);
        crawler.addSeed("http://datahref.com/archives/26");
        /*网页、图片、文件被存储在download文件夹中*/
        /*进行深度为5的爬取*/
        crawler.start(5);
    }
}
