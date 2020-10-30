package com.spider.web;

import com.spider.utils.FileUtils;
import com.spider.utils.OKHttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class Shubao extends BaseWeb{

    public static String listUrl = "http://www.shubao12.cc/qitaxiaoshuo/7_@{page}.html";

    public static String downloadUrl = "http://www.shubao12.cc/modules/article/txtarticle.php?id=@{id}";

    public void getBookList() {
        int page = 388;
        while (true) {
            try {
                String url = listUrl.replace("@{page}", String.valueOf(page));
                logger.info("-----------------{}--------------",url);
                byte[] bytes = OKHttpUtils.getBytes(url);
                String html = new String(bytes, "GBK");
                Document document = Jsoup.parse(html);
                Elements elements = document.getElementById("newscontent").getElementsByClass("l").get(0).getElementsByClass("s2");
                if (elements.isEmpty()||elements.size()==0) {
                    break;
                } else {
                    elements.stream().parallel().forEach(element -> {
                        Elements aList = element.getElementsByTag("a");
                        if (!aList.isEmpty()) {
                            String name =  FileUtils.repairPath(aList.get(0).text());
                            String href = aList.get(0).attr("href");
                            String id = href.split("_")[1].substring(0, href.split("_")[1].length() - 1);
                            String bookUrl = downloadUrl.replace("@{id}", id);
                            byte[] bookBytes = OKHttpUtils.getBytes(bookUrl);
                            FileUtils.byteToFile(bookBytes,"C://书包//"+name+".txt");
                            System.out.println(name+",下载完成");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(page==0){
                break;
            }
            page--;
        }
    }
}
