package com.spider.utils;

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class HtmlCleanerUtil {

    public static HtmlCleaner htmlCleaner = new HtmlCleaner();

    public static TagNode getTagNode(String url, boolean enableProxy) {
        String html = OKHttpUtils.get(url, enableProxy);
        if (StringUtils.isBlank(html)) {
            return null;
        }
        return htmlCleaner.clean(html);
    }

    public static TagNode getTagNode(String html) {
        return htmlCleaner.clean(html);
    }
}
