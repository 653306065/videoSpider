package com.spider.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.spider.entity.By114BT;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import com.spider.service.By114BTService;

@Service
public class By114 {

    private Logger logger = LoggerFactory.getLogger(By114.class);

    @Value("${by114.template}")
    private String template;

    @Value("${by114.home}")
    private String home;

    @Value("${by114.savePath}")
    private String savePath;

    @Autowired
    private By114BTService by114BTService;

    public List<By114BT> getBTInfo(String type, String page) {
        String url = template.replace("@{type}", type).replace("@{page}", page);
        Document document = JsoupUtil.getDocument(url);
        Element element = document.getElementById("threadlisttableid");
        Elements tbodys = element.getElementsByTag("tbody");
        List<By114BT> list = new ArrayList<By114BT>();
        for (Element tb : tbodys) {
            String id = tb.attr("id");
            if (id.startsWith("normalthread")) {
                By114BT by114BT = new By114BT();
                by114BT.setPageHtml(tb.toString());
                String infoUrl = home + tb.getElementsByClass("icn").get(0).getElementsByTag("a").get(0).attr("href");
                Element name = tb.getElementsByClass("new").get(0).getElementsByClass("xst").get(0);
                if (name != null) {
                    by114BT.setUrlName(name.text());
                }
                if ("52".equals(type)) {
                    by114BT.setType("亚洲无码");
                }
                String contentPublishDate = "";
                if (tb.getElementsByClass("by").get(1).getElementsByTag("span") != null) {
                    contentPublishDate = tb.getElementsByClass("by").get(1).getElementsByTag("span").attr("title");
                }
                if (tb.getElementsByClass("by").get(1).getElementsByTag("a") != null) {
                    String text = tb.getElementsByClass("by").get(1).getElementsByTag("a").text();
                    contentPublishDate = text;
                }
                by114BT.setUrl(infoUrl);
                by114BT.setContentPublishDate(contentPublishDate);
                list.add(by114BT);
            }
        }
        return list;
    }

    public void saveBTInfo(By114BT bt) {
        if (by114BTService.count("url", bt.getUrl()) != 0) {
            logger.info(bt.getUrl() + ",已存在");
            return;
        }
        if (by114BTService.count("urlName", bt.getUrlName()) != 0) {
            logger.info(bt.getUrlName() + ",已存在");
            return;
        }
        Document document = JsoupUtil.getDocument(bt.getUrl());
        Element contentHtml = document.getElementsByClass("t_fsz").get(0);
        Element tf = contentHtml.getElementsByClass("t_f").get(0);
        String title = document.getElementById("thread_subject").text();
        bt.setTitle(title);
        bt.setContentHtml(tf.toString());
        // 内容解析
        if (tf.getElementsByTag("font").size() < 3) {
            bt.setContent(tf.text());
        } else {
            Map<String, String> contentMap = new HashMap<String, String>();
            for (Element element : tf.getElementsByTag("font")) {
                if (element.hasText()) {
                    String text = element.text();
                    if (text.indexOf("：") != -1 && text.split("：").length == 2) {
                        String key = text.split("：")[0];
                        String value = text.split("：")[1];
                        contentMap.put(key, value);
                    }
                }
            }
            for (Map.Entry<String, String> entry : contentMap.entrySet()) {
                if ("【影片名称】".equals(entry.getKey())) {
                    bt.setVideoName(entry.getValue());
                }
                if ("【是否有码】".equals(entry.getKey())) {
                    bt.setMosaic(entry.getValue());
                }
                if ("【特 徵 碼】".equals(entry.getKey())) {
                    bt.setMagnet(entry.getValue());
                }
                if ("【影片格式】".equals(entry.getKey())) {
                    bt.setVideoFormat(entry.getValue());
                }
                if ("【影片大小】".equals(entry.getKey())) {
                    bt.setVideoSize(entry.getValue());
                }
                if ("【女 优 】".equals(entry.getKey())) {
                    bt.setActorName(entry.getValue());
                }
                if ("【作种期限】".equals(entry.getKey())) {
                    bt.setBtDeadline(entry.getValue());
                }
            }
        }
        // 图片下载
        List<byte[]> imgList = new ArrayList<byte[]>();
        Elements elements = tf.getElementsByTag("img");
        int index = 0;
        List<String> imgPath = new ArrayList<String>();
        List<String> imgurl = new ArrayList<String>();
        for (Element img : elements) {
            String url = img.attr("src");
            byte[] bytes = OKHttpUtils.getBytes(url);
            if (bytes != null) {
                String path = savePath + "img" + File.separator + FileUtils.repairPath(title) + "_" + index + ".jpg";
                if (bt.getMagnet() != null) {
                    path = savePath + "img" + File.separator + FileUtils.repairPath(title) + "_" + bt.getMagnet() + "_"
                            + index + ".jpg";
                }
                FileUtils.byteToFile(bytes, path);
                imgList.add(bytes);
                imgPath.add(path);
                imgurl.add(url);
                index++;
            }
        }
        bt.setImages(imgList);
        bt.setImagesPath(imgPath);
        bt.setImagesUrl(imgurl);
        Elements element = document.getElementsByClass("attnm");
        if (element != null && element.size() > 0) {
            Element a = element.get(0).getElementsByTag("a").get(0);
            String url = home + a.attr("href");
            String name = a.text();
            byte[] bytes = OKHttpUtils.getBytes(url);
            if (bytes != null) {
                String path = savePath + "torrent" + File.separator + FileUtils.repairPath(bt.getTitle()) + ".torrent";
                FileUtils.byteToFile(bytes, path);
                bt.setTorrent(bytes);
                bt.setTorrentName(name);
                bt.setTorrentPath(path);
                bt.setTorrentUrl(url);
            }
        }
        if (StringUtils.isEmpty(bt.getMagnet()) && Objects.isNull(bt.getTorrent())) {
            logger.info(bt.getTitle() + ",磁力和种子都为空");
            return;
        }
        bt.setCreateData(new Date());
        by114BTService.save(bt);
        logger.info(bt.getTitle() + ",保存完成");
    }

    public void clearFile() {
        File file = new File("D:\\BT");
        File[] list = file.listFiles();
        for (File f : list) {
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (File btFile : files) {
                    if (btFile.isDirectory()) {
                        File[] fileArr = btFile.listFiles();
                        List<File> fileList = new ArrayList<File>();
                        for (File d : fileArr) {
                            if (!d.isDirectory()) {
                                fileList.add(d);
                            } else {
                                FileUtils.deleteDir(d.getAbsolutePath());
                            }
                        }
                        fileList.sort(new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return (int) (o2.length() - o1.length());
                            }
                        });
                        for (int i = 1; i < fileList.size(); i++) {
                            fileList.get(i).delete();
                        }
                    }
                }
            }
        }
    }

    public void deleteFile() {

    }

    public void downloadBt() {
        int i = 1;
        while (true) {
            try {
                List<By114BT> list = getBTInfo("52", String.valueOf(i));
                ExecutorService executorService = Executors.newFixedThreadPool(8);
                // logger.info("list:{}",JSON.toJSONString(list));
                for (By114BT bt : list) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            saveBTInfo(bt);
                        }
                    });
                }
                if (list.size() == 0) {
                    break;
                }
                executorService.shutdown();
                while (true) {
                    if (executorService.isShutdown()) {
                        // executorService.awaitTermination(10,TimeUnit.SECONDS);
                        break;
                    }
                }
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}