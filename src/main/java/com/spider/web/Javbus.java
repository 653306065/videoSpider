package com.spider.web;

import com.spider.entity.ActressesInfo;
import com.spider.service.ActressesInfoService;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import org.dom4j.util.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class Javbus extends BaseWeb {

    @Value("${javbus.home}")
    private String home;

    @Value("${javbus.enableProxy}")
    private Boolean enableProxy;

    @Value("${javbus.thread}")
    private Integer thread;

    @Value("${javbus.actressesListTemplate}")
    private String actressesListTemplate;

    @Value("${javbus.actressesDetailsTemplate}")
    private String actressesDetailsTemplate;

    @Value("${javbus.avTemplate}")
    private String avTemplate;

    @Value("${javbus.savePath}")
    private String savePath;

    @Autowired
    private ActressesInfoService actressesInfoService;

    public List<ActressesInfo> getActressesInfoList(String category, int page) {
        String url = actressesListTemplate.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocumentByProxy(url);
        Elements elements = document.getElementsByClass("avatar-box");
        return elements.stream().map(element -> {
            String infoUrl = element.attr("href");
            ActressesInfo actressesInfo = new ActressesInfo();
            if ("uncensored".equals(category)) {
                actressesInfo.setUncensored(true);
            }
            Elements imgs = element.getElementsByTag("img");
            if (imgs.size() != 0) {
                Element img = imgs.get(0);
                String imgUrl = img.attr("src");
                String name = img.attr("title");
                if (!"https://images.javbus.com/actress/nowprinting.gif".equals(imgUrl)) {
                    byte[] imgBytes = OKHttpUtils.getBytes(imgUrl, enableProxy);
                    if (Objects.nonNull(imgBytes)) {
                        String path = savePath + File.separator + name + ".jpg";
                        FileUtils.byteToFile(imgBytes, path);
                    }
                    actressesInfo.setJavbusPhoto(imgBytes);
                }
                actressesInfo.setJavbusPhotoUrl(imgUrl);
                actressesInfo.setName(name);
                actressesInfo.setJavbusUrl(infoUrl);
                logger.info("{},信息获取完成", name);
            }
            return actressesInfo;
        }).collect(Collectors.toList());
    }

    public void saveAllUncensoredActressesInfo() {
        int page = 1;
        while (true) {
            try {
                List<ActressesInfo> list = getActressesInfoList("uncensored", page);
                if (list == null || list.size() == 0) {
                    break;
                }
                list.stream().filter(info -> Objects.nonNull(info.getName())).forEach(info -> {
                    if (actressesInfoService.count("name", info.getName()) == 0 && actressesInfoService.count("javbusUrl", info.getJavbusUrl()) == 0) {
                        actressesInfoService.insert(info);
                        logger.info("{},保存完成", info.getName());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
        logger.info("信息保存完成");
    }

}
