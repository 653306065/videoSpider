package com.spider.utils;

import java.net.Proxy;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.spider.utils.SpringContentUtil;

public class JsoupUtil {

    private static Proxy proxy = SpringContentUtil.getBean(Proxy.class);

    public static Document getDocument(String url) {
        String html = OKHttpUtils.get(url, false);
        if(StringUtils.isBlank(html)){
            return null;
        }
        Document document = Jsoup.parse(html);
        return document;
    }

    public static Document getDocument(String url,Boolean isProxy) {
        String html = OKHttpUtils.get(url, isProxy);
        if(Objects.isNull(html)){
            return null;
        }
        Document document = Jsoup.parse(html);
        return document;
    }

    public static Document getDocumentByProxy(String url) {
        String html = OKHttpUtils.get(url, true);
        if(StringUtils.isBlank(html)){
            return null;
        }
        Document document = Jsoup.parse(html);
        return document;
    }

    public static Document getDocument(String url, Map<String, String> header,Boolean isProxy) {
        String html = OKHttpUtils.get(url, header, isProxy);
        if(StringUtils.isBlank(html)){
            return null;
        }
        Document document = Jsoup.parse(html);
        return document;
    }
}
