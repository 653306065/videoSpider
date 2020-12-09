package com.spider.web;

import com.alibaba.fastjson.JSON;
import com.spider.entity.CodexInfo;
import com.spider.service.CodexInfoServcie;
import com.spider.utils.JsoupUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class Codex {

    private Logger logger = LoggerFactory.getLogger(By114.class);

    @Autowired
    CodexInfoServcie codexInfoServcie;

    public static String listUrl = "http://codexpcgames.com/page/";

    public List<String> getListUrl(Integer page) {
        Document document = JsoupUtil.getDocument(listUrl + page);
        if(Objects.isNull(document)){
            return null;
        }
        return document.getElementsByClass("post-thumbnail single-post-thumb archive-wide-thumb").stream().map(element -> {
                    if (element.getElementsByTag("a").size() > 0) {
                        return element.getElementsByTag("a").attr("href");
                    }
                    return null;
                }
        ).filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    public CodexInfo getCodexInfo(String url) {
        Document document = JsoupUtil.getDocument(url);
        Elements elements = document.getElementsByAttributeValue("type", "application/ld+json");
        if (CollectionUtils.isNotEmpty(elements)) {
            List<String> aList = document.getElementsByTag("a").stream().filter(element -> StringUtils.isNotBlank(element.attr("href")) && element.attr("href").startsWith("magnet")).map(element -> element.attr("href")).collect(Collectors.toList());
            CodexInfo codexInfo = JSON.parseObject(elements.get(0).data().replace("@", ""), CodexInfo.class);
            if (CollectionUtils.isNotEmpty(aList)) {
                codexInfo.setMagnet(aList.get(0));
            }
            return codexInfo;
        }
        return null;
    }

    public void saveCodexGameInfo() {
        int page = 1;
        while (true) {
            List<String> urlList = getListUrl(page);
            if (CollectionUtils.isEmpty(urlList)) {
                break;
            }
            urlList.stream().parallel().forEach(url -> {
                CodexInfo codexInfo = getCodexInfo(url);
                if (CollectionUtils.isEmpty(codexInfoServcie.findBykeyValue("name", codexInfo.getName()))) {
                    codexInfoServcie.insert(codexInfo);
                    logger.info("{},保存完成", codexInfo.getName());
                }
            });
            page++;
        }
    }

}
