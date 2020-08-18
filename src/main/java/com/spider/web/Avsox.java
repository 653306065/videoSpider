//package com.spider.web;
//
//import java.net.Proxy;
//
//import com.spider.service.AvsoxService;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.alibaba.fastjson.JSON;
//import com.spider.entity.AvInfo;
//import com.spider.utils.DateUtil;
//import com.spider.utils.JsoupUtil;
//import com.spider.utils.OKHttpUtils;
//
//@Service
//public class Avsox {
//
//    private Logger logger = LoggerFactory.getLogger(Avsox.class);
//
//    @Autowired
//    private AvsoxService avsoxService;
//
//    public String actressesListUrl = "https://avsox.asia/cn/actresses/page/@{page}";
//
//    public List<String> getActressesList(String page) {
//        List<String> list = new ArrayList<String>();
//        String realUrl = actressesListUrl.replace("@{page}", page);
//        Document document = JsoupUtil.getDocumentByProxy(realUrl);
//        Elements elements = document.getElementsByClass("avatar-box");
//        for (Element element : elements) {
//            String actressesUrl = element.attr("href");
//            list.add(actressesUrl);
//        }
//        return list;
//    }
//
//    public List<String> getActressesAvList(String url) {
//        List<String> list = new ArrayList<String>();
//        int page = 1;
//        while (true) {
//            String realUrl = url + "/page/" + page;
//            String html = OKHttpUtils.get(realUrl, true);
//            if (html == null) {
//                break;
//            }
//            Document document = Jsoup.parse(html);
//            Elements elements = document.getElementsByClass("movie-box");
//            for (Element element : elements) {
//                String avUrl = element.attr("href");
//                list.add(avUrl);
//            }
//            page++;
//        }
//        return list;
//    }
//
//    public AvInfo getAvInfo(String url) {
//        Document document = JsoupUtil.getDocumentByProxy(url);
//        Elements elements = document.getElementsByClass("info");
//        Element info = elements.get(0);
//        Elements ptags = info.getElementsByTag("p");
//        String code = ptags.get(0).getElementsByTag("span").get(1).text();
//        String dateStr = ptags.get(1).text().replace("发行时间: ", "");
//        int minute = Integer.valueOf(ptags.get(2).text().replace("分钟", "").replace("长度", "").replace(":", "").trim());
//        String maker = ptags.get(4).getElementsByTag("a").text();
//        String cover = document.getElementsByClass("bigImage").get(0).attr("href");
//        Elements tags = ptags.get(ptags.size() - 1).getElementsByTag("span");
//        String tagstr = "";
//        for (Element element : tags) {
//            tagstr = tagstr + "," + element.getElementsByTag("a").get(0).text();
//        }
//        Element avatarsBox = document.getElementById("avatar-waterfall");
//        Elements avatars = avatarsBox.getElementsByClass("avatar-box");
//        String name = "";
//        for (Element element : avatars) {
//            name = name + "," + element.getElementsByTag("span").get(0).text();
//        }
//        AvInfo avInfo = new AvInfo();
//        avInfo.setAvatars(name);
//        avInfo.setCode(code);
//        avInfo.setCover(cover);
//        avInfo.setDate(DateUtil.getDate(dateStr));
//        avInfo.setMaker(maker);
//        avInfo.setMinute(minute);
//        avInfo.setTag(tagstr);
//        avInfo.setUrl(url);
//        return avInfo;
//    }
//
//    public void saveAvInfo() {
//        int page = 1;
//        while (true) {
//            try {
//                List<String> list = getActressesList(String.valueOf(page));
//                logger.info("ActressesList:{}", JSON.toJSONString(list));
//                if (list.size() == 0) {
//                    break;
//                }
//                for (String url : list) {
//                    List<String> avList = getActressesAvList(url);
//                    logger.info("ActressesAvList:{}", JSON.toJSONString(list));
//                    ExecutorService executorService = Executors.newFixedThreadPool(list.size());
//                    for (String avUrl : avList) {
//                        try {
//                            executorService.execute(new Runnable() {
//                                @Override
//                                public void run() {
//                                    logger.info("AvUrl:{}", avUrl);
//                                    AvInfo info = getAvInfo(avUrl);
//                                    logger.info("AvInfo:{}", JSON.toJSONString(info));
//                                    if (avsoxService.findByCode(info.getCode()) == null) {
//                                        avsoxService.insert(info);
//                                    }
//                                }
//                            });
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    executorService.shutdown();
//                    while (true) {
//                        if (executorService.isTerminated()) {
//                            logger.info("线程池执行结束");
//                            break;
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            page++;
//        }
//    }
//}
