package com.spider.web;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.spider.entity.ActressesInfo;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class Xslist extends BaseWeb {

    public static String searchUrl = "https://xslist.org/search?query=@{name}&lg=zh";

    public List<String> getSearchList(String name) {
        String url = searchUrl.replace("@{name}", name);
        Document document = JsoupUtil.getDocument(url);
        if (Objects.isNull(document)) {
            return null;
        }
        return document.getElementsByTag("a").stream().map(element -> element.attr("href")).collect(Collectors.toList());
    }

    public ActressesInfo getInfo(String url) {
        Document document = JsoupUtil.getDocument(url);
        if (Objects.isNull(document)) {
            return null;
        }
        ActressesInfo actressesInfo = new ActressesInfo();
        actressesInfo.setXsLisUrl(url);
        String name = document.getElementById("gallery").getElementsByTag("img").get(0).attr("alt");
        actressesInfo.setName(name);
        List<String> imgList = document.getElementById("gallery").getElementsByTag("img").stream().filter(element -> !"https://xslist.org/assets/images/anonymous2.png".equals(element.attr("src"))).map(element -> element.attr("src")).collect(Collectors.toList());
        actressesInfo.setXsListImageUrlList(imgList);
        List<byte[]> imgByteList = imgList.stream().parallel().map(imgUrl -> {
            byte[] imgByte = OKHttpUtils.getBytes(imgUrl, false);
            if (Objects.nonNull(imgByte)) {
                FileUtils.byteToFile(imgByte, "C:\\xslist\\" + name + "\\" + UUID.randomUUID() + ".jpg");
                return imgByte;
            } else {
                return null;
            }
        }).collect(Collectors.toList());
        actressesInfo.setXsListImageList(imgByteList.stream().filter(Objects::nonNull).collect(Collectors.toList()));
        String h1 = document.getElementById("sss1").getElementsByTag("h1").text();
        actressesInfo.setEnName(h1.replace(name, "").split("/")[0].replace("(", ""));
        List<String> alias = document.getElementsByAttributeValue("itemprop", "additionalName").stream().map(Element::text).collect(Collectors.toList());
        actressesInfo.setAlis(alias);
        List<Element> pList = document.getElementById("layout").getElementsByTag("p");
        List<Element> ps = pList.stream().filter(element -> element.text().contains("出生") || element.text().contains("三围") || element.text().contains("出道日期") || element.text().contains("星座")).limit(1).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(ps)) {
            Element p = ps.get(0);
            String html = p.html();
            Map<String, String> map = new HashMap<>();
            Arrays.stream(html.split("<br>")).forEach(text -> {
                String[] textArr = text.split(":");
                if (textArr.length == 2) {
                    map.put(textArr[0].trim(), textArr[1].trim());
                }
            });
            if (map.containsKey("罩杯") && !map.get("罩杯").equals("n/a")) {
                actressesInfo.setCup(map.get("罩杯"));
            }
            if (map.containsKey("血型") && !map.get("血型").equals("n/a")) {
                actressesInfo.setBloodType(map.get("血型"));
            }
            try {
                if (map.containsKey("出道日期") && !map.get("出道日期").equals("n/a")) {
                    actressesInfo.setDebutDate(DateUtil.parse(String.valueOf(map.get("出道日期")), "yyyy年mm月"));
                }
                if (map.containsKey("出生") && !map.get("出生").equals("n/a")) {
                    actressesInfo.setBirthday(DateUtil.parse(String.valueOf(map.get("出生"))));
                }
            } catch (Exception e) {

            }
            if (map.containsKey("三围") && !map.get("三围").equals("n/a")) {
                actressesInfo.setBWH(map.get("三围"));
            }
            if (map.containsKey("星座") && !map.get("星座").equals("n/a")) {
                actressesInfo.setConstellation(map.get("星座"));
            }
        }
        Elements heights = document.getElementsByAttributeValue("itemprop", "height");
        if (CollectionUtil.isNotEmpty(heights) && !heights.get(0).text().equals("n/a")) {
            actressesInfo.setHeight(Integer.valueOf(heights.get(0).text().replace("cm", "").trim()));
        }
        Elements nationality = document.getElementsByAttributeValue("itemprop", "nationality");
        if (CollectionUtil.isNotEmpty(nationality) && !nationality.get(0).text().equals("n/a")) {
            actressesInfo.setConstellation(nationality.get(0).text());
        }
        return actressesInfo;
    }

}
