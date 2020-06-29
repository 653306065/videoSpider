package com.spider.web;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.spider.utils.OKHttpUtils;

@Component
public class Longteng {

    @Autowired
    private Proxy proxy;

    @Value("${longteng.home}")
    private String home;

    @Value("${longteng.thread}")
    private String thread;

    @Value("${longteng.savePath}")
    private String savePath;

    public List<Map<String, String>> getBookList(String url) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        try {
            byte[] bytes = OKHttpUtils.getBytes(url, proxy);
            String html = new String(bytes, "gbk");
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByClass("table").get(0).getElementsByTag("tr");
            int i = 0;
            for (Element element : elements) {
                if (i != 0) {
                    Element a = element.getElementsByTag("td").get(0).getElementsByTag("a").get(0);
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("url", a.attr("href"));
                    map.put("name", a.attr("title"));
                    list.add(map);
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getChapterList(String url) {
        List<String> list = new ArrayList<String>();
        try {
            byte[] bytes = OKHttpUtils.getBytes(url, proxy);
            String html = new String(bytes, "gbk");
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementById("list-chapterAll").getElementsByTag("dd");
            for (Element element : elements) {
                String chapterUrl = home + element.getElementsByTag("a").get(0).attr("href");
                list.add(chapterUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getChapterText(String url) {
        try {
            byte[] bytes = OKHttpUtils.getBytes(url, proxy);
            String html = new String(bytes, "gbk");
            Document document = Jsoup.parse(html);
            String text = document.getElementById("htmlContent").text();
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    public void downloadBook() {
        int page = 1;
        while (true) {
            List<Map<String, String>> list = getBookList("http://www.longteng12345.com/top/allvisit/" + page + ".html");
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            for (Map<String, String> map : list) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String name = map.get("name");
                            OutputStream outputStream = new FileOutputStream(savePath + "\\" + name + ".txt");
                            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
                            List<String> chapterList = getChapterList(map.get("url"));
                            for (String url : chapterList) {
                                String text = getChapterText(url);
                                bufferedWriter.write(text);
                                bufferedWriter.newLine();
                                bufferedWriter.flush();
                            }
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
            if (list.size() == 0) {
                break;
            }
        }
    }
}
