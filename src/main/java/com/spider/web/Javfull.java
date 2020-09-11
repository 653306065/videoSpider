package com.spider.web;

import com.spider.entity.Video;
import com.spider.utils.OKHttpUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class Javfull extends  BaseWeb{

    @Value("${javfull.home}")
    private String home;

    @Value("${javfull.template}")
    private String template;

    @Value("${javfull.savePath}")
    private String savePath;

    @Value("${javfull.enableProxy}")
    private Boolean enableProxy;

    @Value("${javfull.thread}")
    private Integer thread;

    @Value("${javfull.api}")
    private String api;

    public List<Video> getVideoList(String category,Integer page){
        try {
            String url=template.replace("@{category}",category).replace("@{page}",String.valueOf(page));
            String html=  OKHttpUtils.get(url,enableProxy);
            HtmlCleaner hc = new HtmlCleaner();
            TagNode tn = hc.clean(html);
            String xpath = "//a[@class='hover-posts']/text()";
            Object[] objects = tn.evaluateXPath(xpath);
            for (Object object : objects) {
                System.out.println(object);
            }
            return null;
        }catch (Exception e){
           e.printStackTrace();
        }
        return null;
    }
}
