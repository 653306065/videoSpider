package com.spider.web;

import com.spider.utils.OKHttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Longteng {

    @Value("${longteng.home}")
    private String home;

    @Value("${longteng.thread}")
    private String thread;

    @Value("${longteng.savePath}")
    private String savePath;

    @Value("${longteng.enableProxy}")
    private Boolean enableProxy;

    public List<Map<String, String>> getBookList(String url) {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        try {
            String html = OKHttpUtils.get(url, enableProxy);
            Document document = Jsoup.parse(html);
            Elements elements = document.getElementsByClass("blue");
            for (Element element : elements) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("url",home+element.attr("href"));
                map.put("name", element.text());
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<String> getChapterList(String url) {
        List<String> list = new ArrayList<String>();
        try {
            String html = OKHttpUtils.get(url, enableProxy);
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
            byte[] bytes = OKHttpUtils.getBytes(url, enableProxy);
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
