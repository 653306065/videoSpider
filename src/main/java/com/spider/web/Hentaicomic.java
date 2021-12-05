package com.spider.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.FileUtils;
import com.spider.utils.HtmlCleanerUtil;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.ImageDownload;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Hentaicomic extends BaseWeb {

    @Value("${hentaicomic.updateList}")
    private String updateList;

    @Value("${hentaicomic.tagList}")
    private String tagList;

    @Value("${hentaicomic.xpath.page}")
    private String xpathPage;

    @Value("${hentaicomic.xpath.title}")
    private String xpathTitle;

    @Value("${hentaicomic.xpath.imageList}")
    private String imageList;

    @Value("${hentaicomic.url}")
    private String url;

    @Autowired
    private ImageDownload imageDownload;

    public List<PhotoVo> getUpdateList(Integer page, String tag) {
        String url = updateList.replace("@{page}", String.valueOf(page));
        if (Objects.nonNull(tag)) {
            url = tagList.replace("@{page}", String.valueOf(page)).replace("@{tag}", tag);
        }
        logger.info("page:{}",url);
        TagNode tagNode = HtmlCleanerUtil.getTagNode(url, getEnableProxy());
        if (Objects.isNull(tagNode)) {
            return null;
        }
        try {
            Object[] urlObjects = tagNode.evaluateXPath(xpathPage);
            Object[] titleObjects = tagNode.evaluateXPath(xpathTitle);
            List<String> values = Stream.of(urlObjects).map(o -> o.toString().replace("/photos-index-aid-", "").replace(".html", "")).collect(Collectors.toList());
            List<String> titles = Stream.of(titleObjects).map(o -> FileUtils.repairPath(o.toString())).collect(Collectors.toList());
            List<PhotoVo> list = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                PhotoVo photoVo = new PhotoVo();
                Integer id = Integer.valueOf(values.get(i));
                String title = titles.get(i);
                photoVo.setId(id);
                photoVo.setName(title);
                list.add(photoVo);
            }
            return list;
        } catch (XPatherException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ImageVo> getImageList(Integer id) {
        String realUrl = url.replace("@{id}", String.valueOf(id));
        String text = OKHttpUtils.get(realUrl, getEnableProxy());
        String json = text.split("var imglist = ")[1].split(";")[0].replace("fast_img_host+\\", "").replaceAll("url", "\"url\"").replaceAll("\\\\", "");
        JSONArray jsonArray = JSON.parseArray(json);
        List<ImageVo> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ImageVo imageVo = new ImageVo();
            imageVo.setUrl("https:" + jsonObject.getString("url"));
            imageVo.setName(jsonObject.getString("caption").replace("[", "").replace("]", "") + ".jpg");
            list.add(imageVo);
        }
        return list;
    }

    public void download(String tag) {
        int page = 1;
        while (true) {
            List<PhotoVo> list = getUpdateList(page, tag);
            if(list==null){
                continue;
            }
            if (list.size() == 0) {
                break;
            }
            for(PhotoVo photoVo:list){
                String name = photoVo.getName();
                List<ImageVo> imageVoList = getImageList(photoVo.getId());
                ExecutorService executorService = Executors.newFixedThreadPool(getThread());
                imageVoList.stream().forEach(imageVo -> {
                    executorService.execute(() -> {
                        String savePath = getSavePath() + name + fileSeparator + imageVo.getName();
                        if (Objects.nonNull(tag)) {
                            savePath = getSavePath() + tag + fileSeparator + name + fileSeparator + imageVo.getName();
                        }
                        imageDownload.downloadFile(imageVo.getUrl(), null, savePath, getEnableProxy());
                        //logger.info("{},下载完成", imageVo.getUrl());
                    });
                });
                executorService.shutdown();
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (executorService.isTerminated()) {
                        logger.info("{},下载完成", name);
                        break;
                    }
                }
            }
            page++;
        }
    }

    public void downloadFullColor() {
        download("FullColor");
    }

    @Data
    public static class ImageVo {

        private String url;

        private String name;
    }

    @Data
    public static class PhotoVo {
        private Integer id;

        private String name;
    }
}
